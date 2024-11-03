package dev.slne.surf.cloud.core.netty.network

import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import io.netty.channel.ChannelHandlerContext

object ProtocolSwapHandler {
    fun handleInboundTerminalPacket(context: ChannelHandlerContext, packet: NettyPacket) {
        if (packet.terminal) {
            context.channel().config().setAutoRead(false)
            context.pipeline()
                .addBefore(context.name(), "inbound_config", UnconfiguredPipelineHandler.Inbound())
            context.pipeline().remove(context.name())
        }
    }

    fun handleOutboundTerminalPacket(context: ChannelHandlerContext, packet: NettyPacket) {
        if (packet.terminal) {
            context.pipeline()
                .addAfter(context.name(), "outbound_config", UnconfiguredPipelineHandler.Outbound())
            context.pipeline().remove(context.name())
        }
    }
}
