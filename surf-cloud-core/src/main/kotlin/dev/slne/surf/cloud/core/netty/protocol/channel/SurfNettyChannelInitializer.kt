package dev.slne.surf.cloud.core.netty.protocol.channel

import dev.slne.surf.cloud.api.util.logger
import dev.slne.surf.cloud.core.netty.AbstractNettyBase
import dev.slne.surf.cloud.core.netty.protocol.codec.NettyPacketDecoder
import dev.slne.surf.cloud.core.netty.protocol.codec.NettyPacketEncoder
import dev.slne.surf.cloud.core.netty.protocol.packet.handler.NettyPacketHandler
import io.netty.channel.Channel
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.LengthFieldPrepender
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component

@Component
class SurfNettyChannelInitializer(
    private val modifiers: ObjectProvider<ChannelInitializerModifier>,
    private val context: ConfigurableApplicationContext
) {
    private val log = logger()

    fun initChannel(channel: Channel) {
        val base = context.getBean(AbstractNettyBase::class.java)

        with(channel.pipeline()) {
            addLast("frameDecoder", LengthFieldBasedFrameDecoder(8192, 0, 4, 0, 4))
            addLast("frameEncoder", LengthFieldPrepender(4, false))
            addLast("decoder", NettyPacketDecoder())
            addLast("encoder", NettyPacketEncoder())
            addLast("commonJoinQuitHandler", NettyPacketJoinQuitCommonHandler(base.connection))
            addLast("packetHandler", NettyPacketHandler(base))
            addLast("logger", LoggingHandler(LogLevel.INFO))
            addLast("exceptionHandler", object : ChannelDuplexHandler() {
                override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
                    log.atSevere()
                        .withCause(cause)
                        .log("Exception caught in channel %s", ctx.channel())
                }
            })
        }

        for (modifier in modifiers.orderedStream().toList()) {
            modifier.modify(channel)
        }
    }
}
