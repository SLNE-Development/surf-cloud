package dev.slne.surf.cloud.core.common.netty.network

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.ssl.NotSslRecordException
import io.netty.handler.ssl.SslHandler

class EnforceSslHandler : ChannelInboundHandlerAdapter() {
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        if (msg is ByteBuf) {
            if (!looksLikeSsl(msg)) {
                ctx.close()
                return
            }
        }
        ctx.pipeline().remove(this)
        ctx.fireChannelRead(msg)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable?) {
        if (cause is NotSslRecordException) {
            ctx.close()
        } else {
            ctx.fireExceptionCaught(cause)
        }
    }

    private fun looksLikeSsl(data: ByteBuf): Boolean {
        return data.readableBytes() > 5 && SslHandler.isEncrypted(data, false)
    }
}