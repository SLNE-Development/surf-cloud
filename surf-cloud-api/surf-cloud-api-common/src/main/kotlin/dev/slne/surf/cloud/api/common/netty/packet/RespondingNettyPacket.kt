package dev.slne.surf.cloud.api.common.netty.packet

import dev.slne.surf.cloud.api.common.netty.network.Connection
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readUuid
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.writeUuid
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.surfapi.core.api.util.logger
import io.netty.buffer.ByteBuf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Default timeout duration for standard operations.
 */
val DEFAULT_TIMEOUT = 15.seconds

/**
 * Default timeout duration for urgent operations.
 */
val DEFAULT_URGENT_TIMEOUT = 5.seconds

/**
 * Represents a Netty packet that expects a response.
 *
 * @param P The type of the response packet.
 */
abstract class RespondingNettyPacket<P : ResponseNettyPacket> : NettyPacket() {
    private var uniqueSessionId: UUID? = null

    /**
     * A deferred response object for awaiting the associated response.
     */
    @InternalApi
    val response = CompletableDeferred<P>()

    /**
     * The connection through which the response will be sent.
     */
    @InternalApi
    lateinit var responseConnection: Connection


    /**
     * Fires the packet and awaits its response within the specified timeout.
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
     * @param connection The connection to send the packet through.
     * @return The response packet, or `null` if the timeout elapses.
     */
    suspend fun fireAndAwaitUrgent(connection: Connection): P? =
        fireAndAwait(connection, DEFAULT_URGENT_TIMEOUT)

    /**
     * Fires the packet and awaits its response, throwing an exception if the timeout elapses.
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
     * @param connection The connection to send the packet through.
     * @return The response packet.
     */
    suspend fun fireAndAwaitOrThrowUrgent(connection: Connection): P =
        fireAndAwaitOrThrow(connection, DEFAULT_URGENT_TIMEOUT)

    /**
     * Responds to the packet with the specified response.
     *
     * @param packet The response packet to send.
     */
    fun respond(packet: P) {
        packet.responseTo = uniqueSessionId
            ?: error("Responding packet has no session id. Are you sure it was sent?")
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
    }
}