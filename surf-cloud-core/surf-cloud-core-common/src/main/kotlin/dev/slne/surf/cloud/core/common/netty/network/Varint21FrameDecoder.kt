package dev.slne.surf.cloud.core.common.netty.network

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.types.VarInt
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.CorruptedFrameException
import io.netty.util.ByteProcessor

const val MAX_VARINT21_BYTES = 3

class Varint21FrameDecoder : ByteToMessageDecoder() {

    override fun decode(ctx: ChannelHandlerContext, byteBuf: ByteBuf, out: MutableList<Any>) {
        // if channel is not active just discard the packet
        if (!ctx.channel().isActive) {
            byteBuf.clear()
            return
        }

        // skip any runs of 0x00 we might find
        val packetStart = byteBuf.forEachByte(ByteProcessor.FIND_NON_NUL)
        if (packetStart == -1) {
            byteBuf.clear()
            return
        }
        byteBuf.readerIndex(packetStart)

        // try to read the length of the packet
        byteBuf.markReaderIndex()
        val preIndex = byteBuf.readerIndex()
        val length = readRawVarInt21(byteBuf)
        if (preIndex == byteBuf.readerIndex()) {
            return
        }
        if (length < 0) {
            throw CorruptedFrameException("length wider than 21-bit")
        }

        // note that zero-length packets are ignored
        if (length > 0) {
            if (byteBuf.readableBytes() < length) {
                byteBuf.resetReaderIndex()
            } else {
                out.add(byteBuf.readRetainedSlice(length))
            }
        }


//        byteBuf.markReaderIndex()
//        helperBuf.clear()
//
//        if (!copyVarint(byteBuf, helperBuf)) {
//            byteBuf.resetReaderIndex()
//        } else {
//            val value = VarInt.readVarInt(helperBuf)
//            if (byteBuf.readableBytes() < value) {
//                byteBuf.resetReaderIndex()
//            } else {
//                out.add(byteBuf.readBytes(value))
//            }
//        }
    }

    companion object {
        private fun readRawVarInt21(buffer: ByteBuf): Int {
            if (buffer.readableBytes() < 4) {
                // we don't have enough that we can read a potentially full varint, so fall back to
                // the slow path.
                return readRawVarintSmallBuf(buffer)
            }
            val wholeOrMore = buffer.getIntLE(buffer.readerIndex())

            // take the last three bytes and check if any of them have the high bit set
            val atStop = wholeOrMore.inv() and 0x808080
            if (atStop == 0) {
                throw CorruptedFrameException("length wider than 21-bit")
            }

            val bitsToKeep = Integer.numberOfTrailingZeros(atStop) + 1
            buffer.skipBytes(bitsToKeep shr 3)

            // remove all bits we don't need to keep, a trick from
            // https://github.com/netty/netty/pull/14050#issuecomment-2107750734:
            //
            // > The idea is that thisVarintMask has 0s above the first one of firstOneOnStop, and 1s at
            // > and below it. For example if firstOneOnStop is 0x800080 (where the last 0x80 is the only
            // > one that matters), then thisVarintMask is 0xFF.
            //
            // this is also documented in Hacker's Delight, section 2-1 "Manipulating Rightmost Bits"
            var preservedBytes = wholeOrMore and (atStop xor (atStop - 1))

            // merge together using this trick: https://github.com/netty/netty/pull/14050#discussion_r1597896639
            preservedBytes =
                (preservedBytes and 0x007F007F) or ((preservedBytes and 0x00007F00) shr 1)
            preservedBytes =
                (preservedBytes and 0x00003FFF) or ((preservedBytes and 0x3FFF0000.toInt()) shr 2)
            return preservedBytes
        }

        private fun readRawVarintSmallBuf(buffer: ByteBuf): Int {
            if (!buffer.isReadable) {
                return 0
            }
            buffer.markReaderIndex()

            var tmp = buffer.readByte()
            if (tmp >= 0) {
                return tmp.toInt()
            }
            var result: Int = tmp.toInt() and 0x7F
            if (!buffer.isReadable) {
                buffer.resetReaderIndex()
                return 0
            }

            tmp = buffer.readByte()
            if (tmp >= 0) {
                return result or (tmp.toInt() shl 7)
            }
            result = result or (tmp.toInt() and 0x7F shl 7)
            if (!buffer.isReadable) {
                buffer.resetReaderIndex()
                return 0
            }
            tmp = buffer.readByte()
            if (tmp >= 0) {
                return result or (tmp.toInt() shl 14)
            }

            return result or (tmp.toInt() and 0x7F) shl 14
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