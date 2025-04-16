package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.api.common.netty.network.protocol.await
import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.DisconnectReason
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private val KEEP_ALIVE_TIME = KeepAliveTime(5.seconds)
private val KEEP_ALIVE_LIMIT = KeepAliveTime(10.seconds)

class KeepAliveHandler(
    private val connection: ConnectionImpl,
    private val disconnect: (suspend (reason: DisconnectReason) -> Unit),
    private val isDisconnectProcessed: () -> Boolean,
    private val isClosed: (time: KeepAliveTime) -> Boolean,
    private val getClosedListenerTime: () -> Long
) {
    private var keepAliveTime = KeepAliveTime.now()
    private var keepAlivePending = false
    private var keepAliveChallenge = KeepAliveTime(0)

    var latency = 0
        private set

    suspend fun keepConnectionAlive() {
        val currentTime = KeepAliveTime.now()
        val elapsedTime = currentTime - keepAliveTime

        if (KEEP_ALIVE_TIME.isExpired(elapsedTime)) {
            if (keepAlivePending && !isDisconnectProcessed() && KEEP_ALIVE_LIMIT.isExpired(elapsedTime)) {
                disconnect(DisconnectReason.TIMEOUT)
            } else if (checkIfClosed(currentTime)) {
                keepAlivePending = true
                keepAliveTime = currentTime
                keepAliveChallenge = currentTime
                coroutineScope {
                    launch {
                        val keepAliveId = KeepAlivePacket(keepAliveChallenge.time).await(
                            connection,
                            KEEP_ALIVE_LIMIT.toDuration()
                        )

                        handleKeepAliveResponse(keepAliveId)
                    }
                }
            }
        }
    }

    private suspend fun handleKeepAliveResponse(keepAliveId: Long?) {
        if (keepAlivePending && keepAliveId != null && keepAliveId == keepAliveChallenge.time) {
            val elapsedTime = KeepAliveTime.now() - keepAliveTime

            this.latency = ((latency * 3 + elapsedTime) / 4).toInt()
            this.keepAlivePending = false
        } else {
            disconnect(DisconnectReason.TIMEOUT)
        }
    }

    private suspend fun checkIfClosed(time: KeepAliveTime): Boolean {
        if (isClosed(time)) {
            if (KEEP_ALIVE_TIME.isExpired(time - getClosedListenerTime())) {
                disconnect(DisconnectReason.TIMEOUT)
            }

            return false
        }

        return true
    }
}

/**
 * Inline value class representing a keep-alive timestamp.
 *
 * @property time the timestamp in milliseconds.
 */
@JvmInline
value class KeepAliveTime(val time: Long) {

    /**
     * Checks if the given elapsed time has exceeded this time.
     *
     * @param elapsedTime the elapsed time to check.
     * @return `true` if expired, `false` otherwise.
     */
    fun isExpired(elapsedTime: KeepAliveTime) = elapsedTime >= this

    operator fun compareTo(other: KeepAliveTime) = time.compareTo(other.time)
    operator fun minus(other: KeepAliveTime) = KeepAliveTime(time - other.time)
    operator fun minus(other: Long) = KeepAliveTime(time - other)
    fun toDuration() = time.milliseconds

    companion object {
        /**
         * Returns the current time as a [KeepAliveTime].
         */
        fun now() = KeepAliveTime(System.currentTimeMillis())
    }
}

/**
 * Constructs a [KeepAliveTime] from a [Duration].
 *
 * @param duration the duration to convert.
 * @return the resulting [KeepAliveTime].
 */
fun KeepAliveTime(duration: Duration) = KeepAliveTime(duration.inWholeMilliseconds)

/**
 * Adds a [KeepAliveTime] to an integer value.
 *
 * @receiver the integer value.
 * @param time the [KeepAliveTime] to add.
 * @return the resulting sum.
 */
operator fun Int.plus(time: KeepAliveTime) = this + time.time