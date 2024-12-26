package dev.slne.surf.cloud.core.common.netty.network

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import io.netty.channel.ChannelHandlerContext

object ProtocolSwapHandler {
    fun handleInboundTerminalPacket(context: ChannelHandlerContext, packet: NettyPacket) {
        if (packet.terminal) {
            context.channel().config().isAutoRead = false
            context.pipeline()
                .addBefore(
                    context.name(),
                    HandlerNames.INBOUND_CONFIG,
                    UnconfiguredPipelineHandler.Inbound()
                )
            context.pipeline().remove(context.name())
        }
    }

    fun handleOutboundTerminalPacket(context: ChannelHandlerContext, packet: NettyPacket) {
        if (packet.terminal) {
            context.pipeline()
                .addAfter(
                    context.name(),
                    HandlerNames.OUTBOUND_CONFIG,
                    UnconfiguredPipelineHandler.Outbound()
                )
            context.pipeline().remove(context.name())
        }
    }
}
