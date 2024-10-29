package dev.slne.surf.cloud.core.netty.temp

import dev.slne.surf.cloud.core.netty.common.connection.log
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import io.netty.channel.EventLoop
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration

class NettyConnectionRecoveryHandler(
    private val nettyConnection: AbstractNettyConnection<*, *, *>,
    reconnectInterval: Duration
) : ChannelFutureListener {
    private val reconnectInterval = reconnectInterval.inWholeMilliseconds
    private val shouldDisconnect = AtomicBoolean(false)

    fun stopReconnection() {
        shouldDisconnect.set(true)
    }

    fun resumeReconnect() {
        shouldDisconnect.set(false)
    }


    override fun operationComplete(future: ChannelFuture) {
        val channel = future.channel()

        log.atWarning()
            .log(
                "Connection to %s has been lost, reconnecting in %sms...",
                channel.remoteAddress(),
                reconnectInterval
            )

        channel.disconnect().sync()
        attemptReconnect(channel.eventLoop())
    }

    fun attemptReconnect(eventLoop: EventLoop) {
        eventLoop.schedule({
            log.atInfo()
                .log("Attempting to reconnect to %s...", eventLoop)

            if (shouldDisconnect.get()) {
                log.atInfo()
                    .log("Reconnect request has been cancelled, aborting...")
            } else {
                try {
                    runBlocking { nettyConnection.tryEstablishConnection() }
                } catch (e: Exception) {
                    log.atWarning()
                        .withCause(e)
                        .log(
                            "Failed to reconnect to %s, retrying in %sms...",
                            eventLoop,
                            reconnectInterval
                        )
                    attemptReconnect(eventLoop)
                }
            }
        }, reconnectInterval, TimeUnit.MILLISECONDS)
    }
}