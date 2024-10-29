package dev.slne.surf.cloud.standalone.temp

import dev.slne.surf.cloud.core.netty.client.source.NettyClientSourceImpl
import dev.slne.surf.cloud.core.netty.protocol.channel.ChannelInitializerModifier
import dev.slne.surf.cloud.standalone.netty.server.NettyServerImpl
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("independent")
class NettyPacketJoinQuitIndependentHandler(server: NettyServerImpl) : ChannelInitializerModifier {
    private val container = server.connection

    override fun modify(channel: Channel) {
        channel.pipeline()
            .addBefore("commonJoinQuitHandler", "independentJoinQuitHandler", Handler())
    }

    private inner class Handler : ChannelInboundHandlerAdapter() {
        override fun handlerAdded(ctx: ChannelHandlerContext) {
            val clientSource = NettyClientSourceImpl(container.base, ctx.channel())
            container.clientTracker.addClient(clientSource)
        }

        override fun handlerRemoved(ctx: ChannelHandlerContext) {
            val source = container.source(ctx.channel()) ?: error("source not found")
            container.clientTracker.removeClient(source)
        }
    }
}
