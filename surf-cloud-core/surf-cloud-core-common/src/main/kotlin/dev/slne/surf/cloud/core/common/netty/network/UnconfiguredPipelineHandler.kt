package dev.slne.surf.cloud.core.common.netty.network

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.network.UnconfiguredPipelineHandler.InboundConfigurationTask
import dev.slne.surf.cloud.core.common.netty.network.UnconfiguredPipelineHandler.OutboundConfigurationTask
import io.netty.buffer.ByteBuf
import io.netty.channel.*
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.EncoderException
import io.netty.util.ReferenceCountUtil

object UnconfiguredPipelineHandler {
    fun <T : PacketListener> setupInboundProtocol(newState: ProtocolInfo<T>) =
        setupInboundHandler(PacketDecoder(newState))


    private fun setupInboundHandler(newDecoder: ChannelInboundHandler) =
        InboundConfigurationTask { context ->
            context.pipeline().replace(context.name(), HandlerNames.DECODER, newDecoder)
            context.channel().config().isAutoRead = true
        }


    fun <T : PacketListener> setupOutboundProtocol(newState: ProtocolInfo<T>) =
        setupOutboundHandler(PacketEncoder(newState))


    private fun setupOutboundHandler(newEncoder: ChannelOutboundHandler) =
        OutboundConfigurationTask { it.pipeline().replace(it.name(),
            HandlerNames.ENCODER, newEncoder) }

    class Inbound : ChannelDuplexHandler() {
        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
            if (msg !is ByteBuf && msg !is NettyPacket) {
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
                super.write(channelHandlerContext, msg, channelPromise)
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

        operator fun plus(inboundConfigurationTask: InboundConfigurationTask) = andThen(inboundConfigurationTask)
    }

    class Outbound : ChannelOutboundHandlerAdapter() {
        override fun write(
            channelHandlerContext: ChannelHandlerContext,
            msg: Any,
            channelPromise: ChannelPromise
        ) {
            if (msg is NettyPacket) {
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

        operator fun plus(outboundConfigurationTask: OutboundConfigurationTask) = andThen(outboundConfigurationTask)
    }
}
