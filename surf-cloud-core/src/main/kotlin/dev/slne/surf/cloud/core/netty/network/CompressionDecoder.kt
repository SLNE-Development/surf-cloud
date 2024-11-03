package dev.slne.surf.cloud.core.netty.network

import dev.slne.surf.cloud.api.netty.protocol.buffer.checkDecoded
import dev.slne.surf.cloud.api.netty.protocol.buffer.readVarInt
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import java.nio.ByteBuffer
import java.util.zip.DataFormatException
import java.util.zip.Inflater

class CompressionDecoder(private var threshold: Int, private var validateDecompressed: Boolean) :
    ByteToMessageDecoder() {
    companion object {
        const val MAXIMUM_COMPRESSED_LENGTH = 0x200000
        const val MAXIMUM_UNCOMPRESSED_LENGTH = 0x800000
    }

    private val inflater = Inflater()

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (buf.readableBytes() == 0) return

        val packetSize = buf.readVarInt()

        if (packetSize == 0) {
            out.add(buf.readBytes(buf.readableBytes()))
        } else {
            if (validateDecompressed) {
                checkDecoded(packetSize >= threshold) { "Badly compressed packet - size of $packetSize is below server threshold of $threshold" }
                checkDecoded(packetSize <= MAXIMUM_UNCOMPRESSED_LENGTH) { "Badly compressed packet - size of $packetSize is larger than protocol maximum of $MAXIMUM_UNCOMPRESSED_LENGTH" }
            }

            buf.setupInflaterInput()
            val inflated = inflate(ctx, packetSize)
            inflater.reset()
            out.add(inflated)
        }
    }

    private fun ByteBuf.setupInflaterInput() {
        val byteBuffer: ByteBuffer = if (nioBufferCount() > 0) {
            nioBuffer().also { skipBytes(readableBytes()) }
        } else {
            ByteBuffer.allocateDirect(readableBytes()).apply {
                readBytes(this)
                flip()
            }
        }
        inflater.setInput(byteBuffer)
    }

    @Throws(DataFormatException::class)
    private fun inflate(ctx: ChannelHandlerContext, uncompressedLength: Int): ByteBuf {
        val byteBuf = ctx.alloc().directBuffer(uncompressedLength)

        return try {
            val byteBuffer = byteBuf.internalNioBuffer(0, uncompressedLength)
            val position = byteBuffer.position()

            inflater.inflate(byteBuffer)
            val inflatedLength = byteBuffer.position() - position

            checkDecoded(inflatedLength == uncompressedLength) { "Badly compressed packet - actual length of uncompressed payload $inflatedLength does not match declared size $uncompressedLength" }

            byteBuf.writerIndex(byteBuf.writerIndex() + inflatedLength)
            byteBuf
        } catch (e: Exception) {
            byteBuf.release()
            throw e
        }
    }

    fun setThreshold(threshold: Int, validateDecompressed: Boolean) {
        this.threshold = threshold
        this.validateDecompressed = validateDecompressed
    }
}