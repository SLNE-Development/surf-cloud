package dev.slne.surf.cloud.core.client.netty

import dev.slne.surf.cloud.core.client.netty.state.ConnectionManager
import dev.slne.surf.surfapi.core.api.util.logger
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

class ClientChannelHandler(private val manager: ConnectionManager) :
    ChannelInboundHandlerAdapter() {
    companion object {
        private val log = logger()
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        manager.onChannelInactive(ctx.channel())
        super.channelInactive(ctx)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        manager.onException(ctx.channel(), cause)
        super.exceptionCaught(ctx, cause)
    }
}