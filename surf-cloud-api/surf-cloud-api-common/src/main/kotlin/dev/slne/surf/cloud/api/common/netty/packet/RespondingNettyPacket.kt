package dev.slne.surf.cloud.api.common.netty.packet

import dev.slne.surf.cloud.api.common.netty.exception.RespondingPacketDisconnectedException
import dev.slne.surf.cloud.api.common.netty.network.Connection
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket.Companion.DEFAULT_MAX_ATTEMPTS
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket.Companion.retryFireAndAwait
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readUuid
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.writeUuid
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.surfapi.core.api.util.logger
import io.netty.buffer.ByteBuf
import kotlinx.coroutines.*
import org.springframework.util.backoff.BackOff
import org.springframework.util.backoff.BackOffExecution
import org.springframework.util.backoff.ExponentialBackOff
import java.lang.ref.WeakReference
import java.util.*
import kotlin.properties.Delegates
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Default timeout duration for standard operations.
 *
 * This is used by [RespondingNettyPacket.fireAndAwait] and
 * [RespondingNettyPacket.fireAndAwaitOrThrow] when the caller does not
 * provide an explicit timeout.
 */
val DEFAULT_TIMEOUT = 15.seconds

/**
 * Default timeout duration for urgent operations.
 *
 * This is used by [RespondingNettyPacket.fireAndAwaitUrgent] and
 * [RespondingNettyPacket.fireAndAwaitOrThrowUrgent], typically for
 * latency-sensitive operations where a short timeout is preferable.
 */
val DEFAULT_URGENT_TIMEOUT = 5.seconds

/**
 * Represents a Netty packet that expects a response.
 *
 * A `RespondingNettyPacket` models a *request/response* interaction over a
 * single logical connection:
 *
 * - A unique **session ID** is attached to each request and echoed back
 *   by the corresponding [ResponseNettyPacket].
 * - On the client side, a [CompletableDeferred] is registered in
 *   RespondingPacketSendHandler and completed once the response arrives.
 * - If the underlying connection is closed while waiting, all pending
 *   responding packets are completed exceptionally with
 *   [dev.slne.surf.cloud.api.common.netty.exception.RespondingPacketDisconnectedException] (see `RespondingPacketSendHandler.channelInactive`).
 * - If the connection stays open but no response arrives in time, the
 *   caller will see a regular coroutine [TimeoutCancellationException].
 *
 * Important:
 * - Responding packets are **not safe to replay** automatically across
 *   reconnects. They often represent one-shot commands or queries whose
 *   context on the server may no longer exist after a disconnect.
 * - For this reason, the client must *not* put [RespondingNettyPacket]s
 *   into reconnect queues. Instead, they should fail fast when the
 *   connection is not in a usable state, allowing higher-level code to
 *   decide whether and how to retry.
 *
 * @param P The type of the response packet.
 */
abstract class RespondingNettyPacket<P : ResponseNettyPacket> : NettyPacket() {
    private var uniqueSessionId: UUID? = null

    /**
     * A deferred response object for awaiting the associated response.
     *
     * - When the response arrives, this deferred is completed by
     *   RespondingPacketSendHandler.
     * - If the channel closes while waiting, it is completed exceptionally
     *   with [dev.slne.surf.cloud.api.common.netty.exception.RespondingPacketDisconnectedException].
     * - If the caller uses one of the `fireAndAwait*` methods with a timeout,
     *   a [TimeoutCancellationException] may be thrown if no response is
     *   received within the configured timeout and the connection stays open.
     */
    @InternalApi
    val response by lazy { CompletableDeferred<P>() }

    /**
     * The connection through which the response will be sent.
     *
     * This is stored as a [WeakReference] to avoid retaining the connection
     * instance indefinitely for long-lived packet objects.
     */
    @InternalApi
    private var responseConnection by Delegates.notNull<WeakReference<Connection>>()

    /**
     * Initializes the response connection.
     *
     * Called by the Netty pipeline (see RespondingPacketSendHandler) when
     * a responding packet is first observed on a given channel. The connection
     * reference is then used by [respond] to send the actual response.
     */
    @InternalApi
    fun initResponseConnection(connection: Connection) {
        responseConnection = WeakReference(connection)
    }

    /**
     * Fires the packet and awaits its response within the specified timeout.
     *
     * Behaviour:
     * - If the packet is sent successfully and the response arrives in time,
     *   the corresponding [ResponseNettyPacket] is returned.
     * - If no response arrives within [timeout], this returns `null`.
     * - If the underlying connection is closed while waiting, the underlying
     *   deferred may be completed with [dev.slne.surf.cloud.api.common.netty.exception.RespondingPacketDisconnectedException],
     *   which will propagate to the caller (unless swallowed by a surrounding
     *   `withTimeoutOrNull`).
     *
     * The caller is responsible for handling both:
     * - timeouts (no response in time),
     * - disconnects (channel closed before a response can be delivered).
     *
     * @param connection The connection to send the packet through.
     * @param timeout The timeout duration. Defaults to [DEFAULT_TIMEOUT].
     * @return The response packet, or `null` if the timeout elapses.
     */
    suspend fun fireAndAwait(connection: Connection, timeout: Duration = DEFAULT_TIMEOUT): P? =
        withTimeoutOrNull(timeout) {
            connection.send(this@RespondingNettyPacket)
            response.await()
        }

    /**
     * Fires the packet with an urgent timeout and awaits its response.
     *
     * This is equivalent to calling [fireAndAwait] with [DEFAULT_URGENT_TIMEOUT].
     *
     * @param connection The connection to send the packet through.
     * @return The response packet, or `null` if the timeout elapses.
     */
    suspend fun fireAndAwaitUrgent(connection: Connection): P? =
        fireAndAwait(connection, DEFAULT_URGENT_TIMEOUT)

    /**
     * Fires the packet and awaits its response, throwing an exception if the
     * timeout elapses.
     *
     * Behaviour:
     * - On success, returns the [ResponseNettyPacket].
     * - If no response arrives within [timeout], a [TimeoutCancellationException]
     *   is thrown.
     * - If the underlying channel is closed while waiting, the deferred is
     *   completed exceptionally (typically with [dev.slne.surf.cloud.api.common.netty.exception.RespondingPacketDisconnectedException]),
     *   which is then thrown here as well.
     *
     * The caller can distinguish:
     * - timeout vs.
     * - disconnect (via exception type), and decide whether to retry after
     *   reconnect or fail the operation.
     *
     * @param connection The connection to send the packet through.
     * @param timeout The timeout duration. Defaults to [DEFAULT_TIMEOUT].
     * @return The response packet.
     */
    suspend fun fireAndAwaitOrThrow(
        connection: Connection,
        timeout: Duration = DEFAULT_TIMEOUT
    ): P {
        try {
            return withTimeout(timeout) {
                connection.send(this@RespondingNettyPacket)
                response.await()
            }
        } catch (e: TimeoutCancellationException) {
            log.atWarning()
                .withCause(e.createCopy())
                .log("Timeout while waiting for response to packet ${this::class.simpleName} with session ID $uniqueSessionId")
            throw e
        }
    }

    /**
     * Fires the packet with an urgent timeout and awaits its response, throwing an exception
     * if the timeout elapses.
     *
     * This is equivalent to calling [fireAndAwaitOrThrow] with [DEFAULT_URGENT_TIMEOUT].
     *
     * @param connection The connection to send the packet through.
     * @return The response packet.
     */
    suspend fun fireAndAwaitOrThrowUrgent(connection: Connection): P =
        fireAndAwaitOrThrow(connection, DEFAULT_URGENT_TIMEOUT)

    /**
     * Responds to the packet with the specified response.
     *
     * This method:
     * - assigns the correct [ResponseNettyPacket.responseTo] session ID, and
     * - sends the response via the connection that originally received this
     *   responding packet (see [initResponseConnection]).
     *
     * If the original connection has already been garbage collected or closed:
     * - a warning is logged,
     * - and if the [response] deferred was already initialized, it is completed
     *   exceptionally to signal that the response could not be delivered.
     *
     * @param packet The response packet to send.
     */
    fun respond(packet: P) {
        packet.responseTo = uniqueSessionId
            ?: error("Responding packet has no session id. Are you sure it was sent?")

        val responseConnection = responseConnection.get()
        if (responseConnection == null) {
            log.atWarning()
                .log("Cannot respond to packet ${this::class.simpleName} with session ID $uniqueSessionId: original connection has been garbage collected")

            if ((::response.getDelegate() as Lazy<*>).isInitialized()) {
                response.completeExceptionally(IllegalStateException("Original connection has been garbage collected"))
            }

            return
        }

        responseConnection.send(packet)
    }

    /**
     * Encodes additional fields into the buffer. Internal use only.
     *
     * @param buf The buffer to write to.
     */
    @InternalApi
    fun extraEncode(buf: ByteBuf) {
        buf.writeUuid(getUniqueSessionIdOrCreate())
    }

    /**
     * Decodes additional fields from the buffer. Internal use only.
     *
     * @param buf The buffer to read from.
     */
    @InternalApi
    fun extraDecode(buf: ByteBuf) {
        uniqueSessionId = buf.readUuid()
    }

    /**
     * Retrieves the unique session ID, creating one if it does not exist. Internal use only.
     *
     * @return The unique session ID.
     */
    @InternalApi
    fun getUniqueSessionIdOrCreate(): UUID =
        uniqueSessionId ?: UUID.randomUUID().also { uniqueSessionId = it }

    companion object {
        private val log = logger()

        /**
         * Default max number of attempts for retry helpers.
         */
        private const val DEFAULT_MAX_ATTEMPTS: Int = 5

        /**
         * Creates a default [ExponentialBackOff] instance for retry helpers.
         *
         * This is intentionally conservative: it avoids hammering the server
         * or connection during prolonged outages, while still giving a few
         * quick retry opportunities.
         */
        @JvmStatic
        fun defaultRetryBackOff(): ExponentialBackOff = ExponentialBackOff().apply {
            initialInterval = 500L // 0.5s
            maxInterval = 15_000L  // 15s
            multiplier = 2.0
            maxElapsedTime = 300_000L // stop after ~5 minutes total
        }

        /**
         * Retries sending a responding packet with backoff, returning `null` on
         * overall failure instead of throwing.
         *
         * This helper is designed to handle *connection-level* failures and,
         * optionally, timeouts. It works with a packet factory, because a single
         * [RespondingNettyPacket] instance is tied to one request/response round-trip.
         *
         * Typical behaviour:
         * - On each attempt:
         *   - A fresh packet is created via [packetFactory].
         *   - [operation] is invoked to send it and await the response
         *     (usually [RespondingNettyPacket.fireAndAwait] or
         *     [RespondingNettyPacket.fireAndAwaitOrThrow]).
         * - If the attempt fails with [dev.slne.surf.cloud.api.common.netty.exception.RespondingPacketDisconnectedException],
         *   it is considered retryable if [retryOnDisconnect] is `true`.
         * - If it fails with [TimeoutCancellationException], it is considered
         *   retryable if [retryOnTimeout] is `true`.
         * - Any other exception is treated as non-retryable and is rethrown.
         *
         * @param connectionSupplier supplies the current [Connection] to use.
         *        This is invoked for each attempt to allow to reconnect logic
         *        to provide a fresh connection.
         * @param packetFactory creates a new [RespondingNettyPacket] instance
         *        for each attempt.
         * @param backOff the [org.springframework.util.backoff.BackOff] policy used between attempts.
         * @param maxAttempts maximum number of attempts. If <= 0, [DEFAULT_MAX_ATTEMPTS]
         *        is used.
         * @param retryOnDisconnect whether to retry on [dev.slne.surf.cloud.api.common.netty.exception.RespondingPacketDisconnectedException].
         * @param retryOnTimeout whether to retry on [TimeoutCancellationException].
         * @param operation the actual send/await logic to execute for each attempt.
         *
         * @return the response packet on success, or `null` if all attempts failed
         *         with retryable errors.
         *
         * @throws Throwable if a non-retryable exception occurs on any attempt.
         */
        @JvmStatic
        suspend fun <P : ResponseNettyPacket> retryFireAndAwait(
            connectionSupplier: suspend () -> Connection,
            packetFactory: () -> RespondingNettyPacket<P>,
            backOff: BackOff = defaultRetryBackOff(),
            maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
            retryOnDisconnect: Boolean = true,
            retryOnTimeout: Boolean = false,
            operation: suspend (RespondingNettyPacket<P>, Connection) -> P? =
                { packet, connection -> packet.fireAndAwait(connection) }
        ): P? {
            val effectiveMaxAttempts = if (maxAttempts <= 0) DEFAULT_MAX_ATTEMPTS else maxAttempts
            val execution: BackOffExecution = backOff.start()

            var attempt = 1
            var lastException: Throwable? = null

            while (attempt <= effectiveMaxAttempts) {
                val packet = packetFactory()
                try {
                    val connection = connectionSupplier()
                    return operation(packet, connection)
                } catch (e: Throwable) {
                    val isDisconnect = e is RespondingPacketDisconnectedException
                    val isTimeout = e is TimeoutCancellationException

                    val retriable =
                        (isDisconnect && retryOnDisconnect) ||
                                (isTimeout && retryOnTimeout)

                    if (!retriable) {
                        // Non-retryable error -> propagate immediately.
                        throw e
                    }

                    lastException = e
                    val backOffDelay = execution.nextBackOff()
                    if (backOffDelay == BackOffExecution.STOP) {
                        break
                    }

                    log.atWarning()
                        .withCause(e)
                        .log(
                            "Retryable failure in RespondingNettyPacket (attempt %d/%d), backing off for %d ms",
                            attempt,
                            effectiveMaxAttempts,
                            backOffDelay
                        )

                    delay(backOffDelay.milliseconds)
                    attempt++
                }
            }

            // All attempts used or backoff stopped; log and return null.
            log.atWarning()
                .withCause(lastException)
                .log(
                    "Exhausted retry attempts for RespondingNettyPacket. Returning null response."
                )

            return null
        }

        /**
         * Variant of [retryFireAndAwait] that throws on overall failure instead
         * of returning `null`.
         *
         * This is useful in call sites where a response is mandatory, and the
         * caller wants a single, well-defined failure signal after all retry
         * attempts have been exhausted.
         *
         * @throws Throwable the last encountered exception if all attempts fail,
         *         or a [TimeoutCancellationException]/[RespondingPacketDisconnectedException]
         *         if that was the final, non-retryable failure.
         */
        @JvmStatic
        suspend fun <P : ResponseNettyPacket> retryFireAndAwaitOrThrow(
            connectionSupplier: suspend () -> Connection,
            packetFactory: () -> RespondingNettyPacket<P>,
            backOff: BackOff = defaultRetryBackOff(),
            maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
            retryOnDisconnect: Boolean = true,
            retryOnTimeout: Boolean = false,
            operation: suspend (RespondingNettyPacket<P>, Connection) -> P? =
                { packet, connection -> packet.fireAndAwait(connection) }
        ): P {
            val result = retryFireAndAwait(
                connectionSupplier = connectionSupplier,
                packetFactory = packetFactory,
                backOff = backOff,
                maxAttempts = maxAttempts,
                retryOnDisconnect = retryOnDisconnect,
                retryOnTimeout = retryOnTimeout,
                operation = operation
            )

            if (result != null) {
                return result
            }

            // At this point, either all attempts failed with retryable errors or
            // the backoff policy requested STOP. We throw a generic exception to
            // indicate that no successful response could be obtained.
            throw IllegalStateException("Exhausted retry attempts for RespondingNettyPacket without receiving a response")
        }
    }
}