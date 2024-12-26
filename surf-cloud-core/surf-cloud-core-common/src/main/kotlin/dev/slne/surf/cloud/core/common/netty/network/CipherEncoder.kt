package dev.slne.surf.cloud.core.common.netty.network

import com.velocitypowered.natives.encryption.VelocityCipher
import com.velocitypowered.natives.util.MoreByteBufUtils
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder

class CipherEncoder(private val cipher: VelocityCipher) : MessageToMessageDecoder<ByteBuf>() {
    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
        val compatible = MoreByteBufUtils.ensureCompatible(ctx.alloc(), cipher, msg)

        try {
            cipher.process(compatible)
            out.add(compatible)
        } catch (e: Exception) {
            compatible.release()
            throw e
        }
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext?) {
        cipher.close()
    }
}