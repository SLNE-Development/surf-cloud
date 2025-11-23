package dev.slne.surf.cloud.core.common.netty.network

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.util.netty.UnifiedReadOnlyChannelHandler
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import kotlinx.coroutines.CompletableDeferred
import java.util.*


class RespondingPacketSendHandler : UnifiedReadOnlyChannelHandler<NettyPacket>() {
    private val log = logger()
    private val respondingPackets =
        mutableObject2ObjectMapOf<UUID, CompletableDeferred<ResponseNettyPacket>>()

    @Suppress("DEPRECATION")
    override fun handleRead(
        ctx: ChannelHandlerContext,
        msg: NettyPacket
    ) {
        if (msg is RespondingNettyPacket<*> || msg is ResponseNettyPacket) {
            if (msg.handled) {
                error("Packet $msg was already handled")
            }
            msg.handled()
        }

        if (msg is RespondingNettyPacket<*>) {
            msg.initResponseConnection(
                ctx.channel().attr(ConnectionImpl.CHANNEL_ATTRIBUTE_KEY).get()
            )
        }

        if (msg is ResponseNettyPacket) {
            val responseTo = msg.responseTo
            if (responseTo != null) {
                val deferred = respondingPackets.remove(responseTo)
                deferred?.complete(msg)

                if (deferred == null) {
                    log.atWarning().log("Received response for unknown packet $responseTo")
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun handleWrite(
        ctx: ChannelHandlerContext,
        msg: NettyPacket,
        promise: ChannelPromise
    ) {
        if (msg is RespondingNettyPacket<*> || msg is ResponseNettyPacket) {
            if (msg.handled) {
                error("Packet $msg was already handled")
            }
            msg.handled()
        }

        if (msg is RespondingNettyPacket<*>) {
            respondingPackets[msg.getUniqueSessionIdOrCreate()] =
                msg.response as CompletableDeferred<ResponseNettyPacket>
        }
    }
}