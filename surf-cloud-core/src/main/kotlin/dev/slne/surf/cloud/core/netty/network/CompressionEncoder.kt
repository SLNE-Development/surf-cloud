package dev.slne.surf.cloud.core.netty.network

import dev.slne.surf.cloud.api.netty.protocol.buffer.writeVarInt
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import java.util.zip.Deflater

class CompressionEncoder(private var threshold: Int): MessageToByteEncoder<ByteBuf>() {
    private val encodeBuffer = ByteArray(0x2000)
    private val deflater = Deflater()

    override fun encode(ctx: ChannelHandlerContext, msg: ByteBuf, out: ByteBuf) {
        val readableBytes = msg.readableBytes()

        check(readableBytes <= CompressionDecoder.MAXIMUM_UNCOMPRESSED_LENGTH) { "Packet too big (is $readableBytes, should be less than ${CompressionDecoder.MAXIMUM_UNCOMPRESSED_LENGTH})" }

        if (readableBytes < threshold) {
            out.writeVarInt(0)
            out.writeBytes(msg)

            val bytes = ByteArray(readableBytes)
            msg.readBytes(bytes)
            out.writeVarInt(bytes.size)
            deflater.setInput(bytes, 0, readableBytes)
            deflater.finish()

            while (!deflater.finished()) {
                val compressedLength = deflater.deflate(encodeBuffer)
                out.writeBytes(encodeBuffer, 0, compressedLength)
            }

            deflater.reset()
        }
    }

    fun getThreshold(): Int = threshold

    fun setThreshold(threshold: Int) {
        this.threshold = threshold
    }
}