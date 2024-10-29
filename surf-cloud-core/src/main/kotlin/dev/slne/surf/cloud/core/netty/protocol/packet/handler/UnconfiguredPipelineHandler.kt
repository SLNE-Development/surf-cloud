package dev.slne.surf.cloud.core.netty.protocol.packet.handler

import com.sun.tools.jdi.Packet
import dev.slne.surf.cloud.core.netty.network.PacketDecoder
import dev.slne.surf.cloud.core.netty.network.PacketEncoder
import dev.slne.surf.cloud.core.netty.network.PacketListener
import dev.slne.surf.cloud.core.netty.network.ProtocolInfo
import dev.slne.surf.cloud.core.netty.protocol.packet.handler.UnconfiguredPipelineHandler.InboundConfigurationTask
import dev.slne.surf.cloud.core.netty.protocol.packet.handler.UnconfiguredPipelineHandler.OutboundConfigurationTask
import io.netty.buffer.ByteBuf
import io.netty.channel.*
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.EncoderException
import io.netty.util.ReferenceCountUtil

class UnconfiguredPipelineHandler {
    companion object {
        fun <T : PacketListener> setupInboundProtocol(newState: ProtocolInfo<T>) {
            setupInboundHandler(PacketDecoder(newState))
        }

        private fun setupInboundHandler(newDecoder: ChannelInboundHandler) =
            InboundConfigurationTask { context ->
                context.pipeline().replace(context.name(), "decoder", newDecoder)
                context.channel().config().setAutoRead(true)
            }


        fun <T : PacketListener> setupOutboundProtocol(newState: ProtocolInfo<T>): OutboundConfigurationTask {
            return setupOutboundHandler(PacketEncoder(newState))
        }

        private fun setupOutboundHandler(newEncoder: ChannelOutboundHandler) =
            OutboundConfigurationTask { it.pipeline().replace(it.name(), "encoder", newEncoder) }
    }

    class Inbound : ChannelDuplexHandler() {
        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
            if (msg !is ByteBuf && msg !is Packet) {
                ctx.fireChannelRead(msg)
            } else {
                ReferenceCountUtil.release(msg)
                throw DecoderException("Pipeline has no inbound protocol configured, can't process packet $msg")
            }
        }

        override fun write(
            channelHandlerContext: ChannelHandlerContext,
            msg: Any,
            channelPromise: ChannelPromise
        ) {
            if (msg is InboundConfigurationTask) {
                try {
                    msg.run(channelHandlerContext)
                } finally {
                    ReferenceCountUtil.release(msg)
                }

                channelPromise.setSuccess()
            } else {
                channelHandlerContext.write(msg, channelPromise)
            }
        }
    }

    @FunctionalInterface
    fun interface InboundConfigurationTask {
        fun run(context: ChannelHandlerContext)

        fun andThen(inboundConfigurationTask: InboundConfigurationTask) =
            InboundConfigurationTask { context ->
                run(context)
                inboundConfigurationTask.run(context)
            }
    }

    class Outbound : ChannelOutboundHandlerAdapter() {
        override fun write(
            channelHandlerContext: ChannelHandlerContext,
            msg: Any,
            channelPromise: ChannelPromise
        ) {
            if (msg is Packet) {
                ReferenceCountUtil.release(msg)
                throw EncoderException("Pipeline has no outbound protocol configured, can't process packet $msg")
            } else {
                if (msg is OutboundConfigurationTask) {
                    try {
                        msg.run(channelHandlerContext)
                    } finally {
                        ReferenceCountUtil.release(msg)
                    }

                    channelPromise.setSuccess()
                } else {
                    channelHandlerContext.write(msg, channelPromise)
                }
            }
        }
    }

    @FunctionalInterface
    fun interface OutboundConfigurationTask {
        fun run(context: ChannelHandlerContext)

        fun andThen(outboundConfigurationTask: OutboundConfigurationTask) =
            OutboundConfigurationTask { context ->
                this.run(context)
                outboundConfigurationTask.run(context)
            }
    }
}
