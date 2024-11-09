package dev.slne.surf.cloud.core.netty.network

import dev.slne.surf.cloud.api.netty.protocol.buffer.types.VarInt
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.CorruptedFrameException

const val MAX_VARINT21_BYTES = 3

class Varint21FrameDecoder : ByteToMessageDecoder() {

    private val helperBuf = Unpooled.directBuffer(MAX_VARINT21_BYTES)

    override fun handlerRemoved0(ctx: ChannelHandlerContext?) {
        helperBuf.release()
    }

    override fun decode(ctx: ChannelHandlerContext, byteBuf: ByteBuf, out: MutableList<Any>) {
        // if channel is not active just discard the packet
        if (!ctx.channel().isActive) {
            byteBuf.skipBytes(byteBuf.readableBytes())
            return
        }

        byteBuf.markReaderIndex()
        helperBuf.clear()

        if (!copyVarint(byteBuf, helperBuf)) {
            byteBuf.resetReaderIndex()
        } else {
            val value = VarInt.readVarInt(helperBuf)
            if (byteBuf.readableBytes() < value) {
                byteBuf.resetReaderIndex()
            } else {
                out.add(byteBuf.readBytes(value))
            }
        }
    }
}

private fun copyVarint(source: ByteBuf, sizeBuf: ByteBuf): Boolean {
    for (i in 0..2) {
        if (!source.isReadable) return false

        val byte = source.readByte()
        sizeBuf.writeByte(byte.toInt())

        if (!VarInt.hasContinuationBit(byte)) return true
    }

    throw CorruptedFrameException("length wider than 21-bit")
}