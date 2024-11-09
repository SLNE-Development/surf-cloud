package dev.slne.surf.cloud.core.netty.network

import dev.slne.surf.cloud.api.netty.protocol.buffer.types.VarInt
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.EncoderException
import io.netty.handler.codec.MessageToByteEncoder

@Sharable
class Varint21LengthFieldPrepender: MessageToByteEncoder<ByteBuf>() {
    override fun encode(ctx: ChannelHandlerContext, msg: ByteBuf, out: ByteBuf) {
        val readableBytes = msg.readableBytes()
        val size = VarInt.getEncodedSize(readableBytes)

        if (size > MAX_VARINT21_BYTES) {
            throw EncoderException("Packet too large: size $size is over 8")
        }

        out.ensureWritable(size + readableBytes)
        VarInt.writeVarInt(out, readableBytes)
        out.writeBytes(msg, msg.readerIndex(), readableBytes)
    }
}