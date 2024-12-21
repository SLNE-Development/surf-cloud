package dev.slne.surf.cloud.core.common.netty.network

import com.velocitypowered.natives.encryption.VelocityCipher
import com.velocitypowered.natives.util.MoreByteBufUtils
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.readVarInt
import dev.slne.surf.cloud.api.common.util.logger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder

class CipherDecoder(private val cipher: VelocityCipher) : MessageToMessageDecoder<ByteBuf>() {

    val log = logger()

    @OptIn(ExperimentalStdlibApi::class)
    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
        val compatible = MoreByteBufUtils.ensureCompatible(ctx.alloc(), cipher, msg)

        runCatching {
            cipher.process(compatible)
            out.add(compatible)
        }.onFailure {
            compatible.release()
            log.atSevere()
                .withCause(it)
                .log("Failed to decode message $msg")
            throw it
        }
    }

    override fun handlerRemoved(ctx: ChannelHandlerContext?) {
        cipher.close()
    }
}