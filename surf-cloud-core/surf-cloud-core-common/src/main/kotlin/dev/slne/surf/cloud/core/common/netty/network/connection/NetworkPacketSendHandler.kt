package dev.slne.surf.cloud.core.common.netty.network.connection

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.network.DisconnectReason
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.surfapi.core.api.util.logger
import io.netty.channel.ChannelFutureListener
import kotlinx.coroutines.CompletableDeferred

internal class NetworkPacketSendHandler(private val connection: ConnectionImpl) {

    fun sendPacketNow(
        packet: NettyPacket,
        flush: Boolean,
        deferred: CompletableDeferred<Boolean>? = null,
        sendListener: ChannelFutureListener? = null
    ) {
        connection.stats.onPacketSent()
        val channel = connection._channel ?: return
        val eventLoop = channel.eventLoop()

        if (eventLoop.inEventLoop()) {
            doSendPacket(packet, flush, deferred, sendListener)
        } else {
            eventLoop.execute { doSendPacket(packet, flush, deferred, sendListener) }
        }
    }

    private fun doSendPacket(
        packet: NettyPacket,
        flush: Boolean,
        deferred: CompletableDeferred<Boolean>? = null,
        sendListener: ChannelFutureListener? = null
    ) {
        if (!connection.connected) {
            deferred?.complete(false)
            return
        }

        try {
            val channel = connection.channel
            val channelFuture = if (flush) channel.writeAndFlush(packet) else channel.write(packet)

            if (deferred != null) {
                channelFuture.addListener { future ->
                    if (future.isSuccess) {
                        deferred.complete(true)
                    } else {
                        deferred.completeExceptionally(future.cause())
                    }
                }
            }

            if (sendListener != null) channelFuture.addListener(sendListener)

            channelFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
        } catch (e: Exception) {
            log.atSevere().withCause(e).log("NetworkException: ")
            deferred?.completeExceptionally(e)
            connection.disconnect(DisconnectionDetails(DisconnectReason.INTERNAL_EXCEPTION, e.message))
        }
    }

    companion object {
        private val log = logger()
    }
}