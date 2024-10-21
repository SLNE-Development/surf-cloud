package dev.slne.surf.cloud.standalone.netty.server.protocol.packet.handler

import dev.slne.surf.cloud.core.netty.protocol.channel.ChannelInitializerModifier
import dev.slne.surf.cloud.core.netty.protocol.packets.ProxiedNettyPacket
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.socket.SocketChannel
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("independent")
class NettyServerProxiedPacketHandler : ChannelInitializerModifier {
    override fun modify(channel: SocketChannel) {
        channel.pipeline().addBefore("packetHandler", "proxiedPacketHandler", Handler())
    }

    private class Handler : ChannelDuplexHandler() {
        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
            if (msg is ProxiedNettyPacket) {
                msg.target.sendPacket(msg)
            } else {
                super.channelRead(ctx, msg)
            }
        }
    }
}
