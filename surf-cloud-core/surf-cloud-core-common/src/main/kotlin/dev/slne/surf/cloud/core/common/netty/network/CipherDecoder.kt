package dev.slne.surf.cloud.core.common.netty.network

import com.velocitypowered.natives.encryption.VelocityCipher
import com.velocitypowered.natives.util.MoreByteBufUtils
import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.core.common.util.encryption.Crypt
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder

class CipherDecoder(private val cipher: VelocityCipher) : MessageToMessageDecoder<ByteBuf>() {

    val log = logger()

    @OptIn(ExperimentalStdlibApi::class)
    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
        val compatible = MoreByteBufUtils.ensureCompatible(ctx.alloc(), cipher, msg).slice()

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