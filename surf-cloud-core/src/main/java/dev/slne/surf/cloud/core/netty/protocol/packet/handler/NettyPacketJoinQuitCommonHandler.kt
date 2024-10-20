package dev.slne.surf.cloud.core.netty.protocol.packet.handler

import dev.slne.surf.cloud.core.netty.common.connection.AbstractNettyConnection
import dev.slne.surf.cloud.core.netty.protocol.packets.server.ClientJoinNettyPacket
import dev.slne.surf.cloud.core.netty.protocol.packets.server.ClientQuitPacket
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

class NettyPacketJoinQuitCommonHandler(private val container: AbstractNettyConnection<*, *, *>) :
    ChannelInboundHandlerAdapter() {

    override fun handlerAdded(ctx: ChannelHandlerContext) = with(container) {
        val source = source(ctx.channel()) ?: return
        base.onPacketReceived(ClientJoinNettyPacket(), source, null)
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext) = with(container) {
        val source = source(ctx.channel()) ?: return
        base.onPacketReceived(ClientQuitPacket(), source, null)
    }

    override fun channelUnregistered(ctx: ChannelHandlerContext) {
        super.channelUnregistered(ctx)
    }
}
