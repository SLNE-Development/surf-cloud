package dev.slne.surf.cloud.api.netty.protocol.buffer

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.util.ByteProcessor
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.channels.GatheringByteChannel
import java.nio.channels.ScatteringByteChannel
import java.nio.charset.Charset

/**
 * A wrapper class for Netty's ByteBuf that delegates all operations to the underlying ByteBuf instance.
 * This class is used to add additional functionality or to monitor/intercept ByteBuf operations.
 *
 * @property buf The underlying ByteBuf instance.
 */
open class WrappedByteBuf(private val buf: ByteBuf) : ByteBuf() {
    override fun alloc(): ByteBufAllocator = buf.alloc()
    override fun array(): ByteArray = buf.array()
    override fun arrayOffset(): Int = buf.arrayOffset()
    override fun asByteBuf(): ByteBuf = buf.asByteBuf()
    override fun asReadOnly(): ByteBuf = buf.asReadOnly()
    override fun bytesBefore(index: Int, length: Int, value: Byte): Int =
        buf.bytesBefore(index, length, value)

    override fun bytesBefore(length: Int, value: Byte): Int = buf.bytesBefore(length, value)
    override fun bytesBefore(value: Byte): Int = buf.bytesBefore(value)
    override fun capacity(): Int = buf.capacity()
    override fun capacity(newCapacity: Int): ByteBuf = buf.capacity(newCapacity)
    override fun clear(): ByteBuf = buf.clear()
    override fun compareTo(buffer: ByteBuf): Int = buf.compareTo(buffer)
    override fun copy(): ByteBuf = buf.copy()
    override fun copy(index: Int, length: Int): ByteBuf = buf.copy(index, length)
    override fun discardReadBytes(): ByteBuf = buf.discardReadBytes()
    override fun discardSomeReadBytes(): ByteBuf = buf.discardSomeReadBytes()
    override fun duplicate(): ByteBuf = buf.duplicate()
    override fun ensureWritable(minWritableBytes: Int): ByteBuf =
        buf.ensureWritable(minWritableBytes)

    override fun ensureWritable(minWritableBytes: Int, force: Boolean): Int =
        buf.ensureWritable(minWritableBytes, force)

    override fun equals(other: Any?): Boolean = buf == other
    override fun forEachByte(index: Int, length: Int, processor: ByteProcessor): Int =
        buf.forEachByte(index, length, processor)

    override fun forEachByte(processor: ByteProcessor): Int = buf.forEachByte(processor)
    override fun forEachByteDesc(index: Int, length: Int, processor: ByteProcessor): Int =
        buf.forEachByteDesc(index, length, processor)

    override fun forEachByteDesc(processor: ByteProcessor): Int = buf.forEachByteDesc(processor)
    override fun getBoolean(index: Int): Boolean = buf.getBoolean(index)
    override fun getByte(index: Int): Byte = buf.getByte(index)
    override fun getBytes(index: Int, dst: ByteArray): ByteBuf = buf.getBytes(index, dst)
    override fun getBytes(index: Int, dst: ByteArray, dstIndex: Int, length: Int): ByteBuf =
        buf.getBytes(index, dst, dstIndex, length)

    override fun getBytes(index: Int, dst: ByteBuf): ByteBuf = buf.getBytes(index, dst)
    override fun getBytes(index: Int, dst: ByteBuf, dstIndex: Int, length: Int): ByteBuf =
        buf.getBytes(index, dst, dstIndex, length)

    override fun getBytes(index: Int, dst: ByteBuf, length: Int): ByteBuf =
        buf.getBytes(index, dst, length)

    override fun getBytes(index: Int, dst: ByteBuffer): ByteBuf = buf.getBytes(index, dst)

    @Throws(IOException::class)
    override fun getBytes(index: Int, out: FileChannel, position: Long, length: Int): Int =
        buf.getBytes(index, out, position, length)

    @Throws(IOException::class)
    override fun getBytes(index: Int, out: GatheringByteChannel, length: Int): Int =
        buf.getBytes(index, out, length)

    @Throws(IOException::class)
    override fun getBytes(index: Int, out: OutputStream, length: Int): ByteBuf =
        buf.getBytes(index, out, length)

    override fun getChar(index: Int): Char = buf.getChar(index)
    override fun getCharSequence(index: Int, length: Int, charset: Charset): CharSequence =
        buf.getCharSequence(index, length, charset)

    override fun getDouble(index: Int): Double = buf.getDouble(index)
    override fun getDoubleLE(index: Int): Double = buf.getDoubleLE(index)
    override fun getFloat(index: Int): Float = buf.getFloat(index)
    override fun getFloatLE(index: Int): Float = buf.getFloatLE(index)
    override fun getInt(index: Int): Int = buf.getInt(index)
    override fun getIntLE(index: Int): Int = buf.getIntLE(index)
    override fun getLong(index: Int): Long = buf.getLong(index)
    override fun getLongLE(index: Int): Long = buf.getLongLE(index)
    override fun getMedium(index: Int): Int = buf.getMedium(index)
    override fun getMediumLE(index: Int): Int = buf.getMediumLE(index)
    override fun getShort(index: Int): Short = buf.getShort(index)
    override fun getShortLE(index: Int): Short = buf.getShortLE(index)
    override fun getUnsignedByte(index: Int): Short = buf.getUnsignedByte(index)
    override fun getUnsignedInt(index: Int): Long = buf.getUnsignedInt(index)
    override fun getUnsignedIntLE(index: Int): Long = buf.getUnsignedIntLE(index)
    override fun getUnsignedMedium(index: Int): Int = buf.getUnsignedMedium(index)
    override fun getUnsignedMediumLE(index: Int): Int = buf.getUnsignedMediumLE(index)
    override fun getUnsignedShort(index: Int): Int = buf.getUnsignedShort(index)
    override fun getUnsignedShortLE(index: Int): Int = buf.getUnsignedShortLE(index)
    override fun hasArray(): Boolean = buf.hasArray()
    override fun hashCode(): Int = buf.hashCode()
    override fun hasMemoryAddress(): Boolean = buf.hasMemoryAddress()
    override fun indexOf(fromIndex: Int, toIndex: Int, value: Byte): Int =
        buf.indexOf(fromIndex, toIndex, value)

    override fun internalNioBuffer(index: Int, length: Int): ByteBuffer =
        buf.internalNioBuffer(index, length)

    override fun isContiguous(): Boolean = buf.isContiguous
    override fun isDirect(): Boolean = buf.isDirect
    override fun isReadable(): Boolean = buf.isReadable
    override fun isReadable(size: Int): Boolean = buf.isReadable(size)
    override fun isReadOnly(): Boolean = buf.isReadOnly
    override fun isWritable(): Boolean = buf.isWritable
    override fun isWritable(size: Int): Boolean = buf.isWritable(size)
    override fun markReaderIndex(): ByteBuf = buf.markReaderIndex()
    override fun markWriterIndex(): ByteBuf = buf.markWriterIndex()
    override fun maxCapacity(): Int = buf.maxCapacity()
    override fun maxFastWritableBytes(): Int = buf.maxFastWritableBytes()
    override fun maxWritableBytes(): Int = buf.maxWritableBytes()
    override fun memoryAddress(): Long = buf.memoryAddress()
    override fun nioBuffer(): ByteBuffer = buf.nioBuffer()
    override fun nioBuffer(index: Int, length: Int): ByteBuffer = buf.nioBuffer(index, length)
    override fun nioBufferCount(): Int = buf.nioBufferCount()
    override fun nioBuffers(): Array<ByteBuffer> = buf.nioBuffers()
    override fun nioBuffers(index: Int, length: Int): Array<ByteBuffer> =
        buf.nioBuffers(index, length)

    @Deprecated("")
    override fun order(): ByteOrder = buf.order()

    @Deprecated("")
    override fun order(endianness: ByteOrder): ByteBuf = buf.order(endianness)
    override fun readableBytes(): Int = buf.readableBytes()
    override fun readBoolean(): Boolean = buf.readBoolean()
    override fun readByte(): Byte = buf.readByte()
    override fun readBytes(dst: ByteArray): ByteBuf = buf.readBytes(dst)
    override fun readBytes(dst: ByteArray, dstIndex: Int, length: Int): ByteBuf =
        buf.readBytes(dst, dstIndex, length)

    override fun readBytes(dst: ByteBuf): ByteBuf = buf.readBytes(dst)
    override fun readBytes(dst: ByteBuf, dstIndex: Int, length: Int): ByteBuf =
        buf.readBytes(dst, dstIndex, length)

    override fun readBytes(dst: ByteBuf, length: Int): ByteBuf = buf.readBytes(dst, length)
    override fun readBytes(dst: ByteBuffer): ByteBuf = buf.readBytes(dst)
    override fun readBytes(length: Int): ByteBuf = buf.readBytes(length)

    @Throws(IOException::class)
    override fun readBytes(out: FileChannel, position: Long, length: Int): Int =
        buf.readBytes(out, position, length)

    @Throws(IOException::class)
    override fun readBytes(out: GatheringByteChannel, length: Int): Int = buf.readBytes(out, length)

    @Throws(IOException::class)
    override fun readBytes(out: OutputStream, length: Int): ByteBuf = buf.readBytes(out, length)
    override fun readChar(): Char = buf.readChar()
    override fun readCharSequence(length: Int, charset: Charset): CharSequence =
        buf.readCharSequence(length, charset)

    override fun readDouble(): Double = buf.readDouble()
    override fun readDoubleLE(): Double = buf.readDoubleLE()
    override fun readerIndex(): Int = buf.readerIndex()
    override fun readerIndex(readerIndex: Int): ByteBuf = buf.readerIndex(readerIndex)
    override fun readFloat(): Float = buf.readFloat()
    override fun readFloatLE(): Float = buf.readFloatLE()
    override fun readInt(): Int = buf.readInt()
    override fun readIntLE(): Int = buf.readIntLE()
    override fun readLong(): Long = buf.readLong()
    override fun readLongLE(): Long = buf.readLongLE()
    override fun readMedium(): Int = buf.readMedium()
    override fun readMediumLE(): Int = buf.readMediumLE()
    override fun readRetainedSlice(length: Int): ByteBuf = buf.readRetainedSlice(length)
    override fun readShort(): Short = buf.readShort()
    override fun readShortLE(): Short = buf.readShortLE()
    override fun readSlice(length: Int): ByteBuf = buf.readSlice(length)
    override fun readUnsignedByte(): Short = buf.readUnsignedByte()
    override fun readUnsignedInt(): Long = buf.readUnsignedInt()
    override fun readUnsignedIntLE(): Long = buf.readUnsignedIntLE()
    override fun readUnsignedMedium(): Int = buf.readUnsignedMedium()
    override fun readUnsignedMediumLE(): Int = buf.readUnsignedMediumLE()
    override fun readUnsignedShort(): Int = buf.readUnsignedShort()
    override fun readUnsignedShortLE(): Int = buf.readUnsignedShortLE()
    override fun resetReaderIndex(): ByteBuf = buf.resetReaderIndex()
    override fun resetWriterIndex(): ByteBuf = buf.resetWriterIndex()
    override fun retain(): ByteBuf = buf.retain()
    override fun retain(increment: Int): ByteBuf = buf.retain(increment)
    override fun retainedDuplicate(): ByteBuf = buf.retainedDuplicate()
    override fun retainedSlice(): ByteBuf = buf.retainedSlice()
    override fun retainedSlice(index: Int, length: Int): ByteBuf = buf.retainedSlice(index, length)
    override fun setBoolean(index: Int, value: Boolean): ByteBuf = buf.setBoolean(index, value)
    override fun setByte(index: Int, value: Int): ByteBuf = buf.setByte(index, value)

    @Throws(IOException::class)
    override fun setBytes(index: Int, `in`: FileChannel, position: Long, length: Int): Int =
        buf.setBytes(index, `in`, position, length)

    @Throws(IOException::class)
    override fun setBytes(index: Int, `in`: InputStream, length: Int): Int =
        buf.setBytes(index, `in`, length)

    @Throws(IOException::class)
    override fun setBytes(index: Int, `in`: ScatteringByteChannel, length: Int): Int =
        buf.setBytes(index, `in`, length)

    override fun setBytes(index: Int, src: ByteArray): ByteBuf = buf.setBytes(index, src)
    override fun setBytes(index: Int, src: ByteArray, srcIndex: Int, length: Int): ByteBuf =
        buf.setBytes(index, src, srcIndex, length)

    override fun setBytes(index: Int, src: ByteBuf): ByteBuf = buf.setBytes(index, src)
    override fun setBytes(index: Int, src: ByteBuf, length: Int): ByteBuf =
        buf.setBytes(index, src, length)

    override fun setBytes(index: Int, src: ByteBuf, srcIndex: Int, length: Int): ByteBuf =
        buf.setBytes(index, src, srcIndex, length)

    override fun setBytes(index: Int, src: ByteBuffer): ByteBuf = buf.setBytes(index, src)
    override fun setChar(index: Int, value: Int): ByteBuf = buf.setChar(index, value)
    override fun setCharSequence(index: Int, sequence: CharSequence, charset: Charset): Int =
        buf.setCharSequence(index, sequence, charset)

    override fun setDouble(index: Int, value: Double): ByteBuf = buf.setDouble(index, value)
    override fun setDoubleLE(index: Int, value: Double): ByteBuf = buf.setDoubleLE(index, value)
    override fun setFloat(index: Int, value: Float): ByteBuf = buf.setFloat(index, value)
    override fun setFloatLE(index: Int, value: Float): ByteBuf = buf.setFloatLE(index, value)
    override fun setIndex(readerIndex: Int, writerIndex: Int): ByteBuf =
        buf.setIndex(readerIndex, writerIndex)

    override fun setInt(index: Int, value: Int): ByteBuf = buf.setInt(index, value)
    override fun setIntLE(index: Int, value: Int): ByteBuf = buf.setIntLE(index, value)
    override fun setLong(index: Int, value: Long): ByteBuf = buf.setLong(index, value)
    override fun setLongLE(index: Int, value: Long): ByteBuf = buf.setLongLE(index, value)
    override fun setMedium(index: Int, value: Int): ByteBuf = buf.setMedium(index, value)
    override fun setMediumLE(index: Int, value: Int): ByteBuf = buf.setMediumLE(index, value)
    override fun setShort(index: Int, value: Int): ByteBuf = buf.setShort(index, value)
    override fun setShortLE(index: Int, value: Int): ByteBuf = buf.setShortLE(index, value)
    override fun setZero(index: Int, length: Int): ByteBuf = buf.setZero(index, length)
    override fun skipBytes(length: Int): ByteBuf = buf.skipBytes(length)
    override fun slice(): ByteBuf = buf.slice()
    override fun slice(index: Int, length: Int): ByteBuf = buf.slice(index, length)
    override fun toString(): String = buf.toString()
    override fun toString(charset: Charset): String = buf.toString(charset)
    override fun toString(index: Int, length: Int, charset: Charset): String =
        buf.toString(index, length, charset)

    override fun touch(): ByteBuf = buf.touch()
    override fun touch(hint: Any): ByteBuf = buf.touch(hint)
    override fun unwrap(): ByteBuf = buf.unwrap()
    override fun writableBytes(): Int = buf.writableBytes()
    override fun writeBoolean(value: Boolean): ByteBuf = buf.writeBoolean(value)
    override fun writeByte(value: Int): ByteBuf = buf.writeByte(value)

    @Throws(IOException::class)
    override fun writeBytes(`in`: FileChannel, position: Long, length: Int): Int =
        buf.writeBytes(`in`, position, length)

    @Throws(IOException::class)
    override fun writeBytes(`in`: InputStream, length: Int): Int = buf.writeBytes(`in`, length)

    @Throws(IOException::class)
    override fun writeBytes(`in`: ScatteringByteChannel, length: Int): Int =
        buf.writeBytes(`in`, length)

    override fun writeBytes(src: ByteArray): ByteBuf = buf.writeBytes(src)
    override fun writeBytes(src: ByteArray, srcIndex: Int, length: Int): ByteBuf =
        buf.writeBytes(src, srcIndex, length)

    override fun writeBytes(src: ByteBuf): ByteBuf = buf.writeBytes(src)
    override fun writeBytes(src: ByteBuf, length: Int): ByteBuf = buf.writeBytes(src, length)
    override fun writeBytes(src: ByteBuf, srcIndex: Int, length: Int): ByteBuf =
        buf.writeBytes(src, srcIndex, length)

    override fun writeBytes(src: ByteBuffer): ByteBuf = buf.writeBytes(src)
    override fun writeChar(value: Int): ByteBuf = buf.writeChar(value)
    override fun writeCharSequence(sequence: CharSequence, charset: Charset): Int =
        buf.writeCharSequence(sequence, charset)

    override fun writeDouble(value: Double): ByteBuf = buf.writeDouble(value)
    override fun writeDoubleLE(value: Double): ByteBuf = buf.writeDoubleLE(value)
    override fun writeFloat(value: Float): ByteBuf = buf.writeFloat(value)
    override fun writeFloatLE(value: Float): ByteBuf = buf.writeFloatLE(value)
    override fun writeInt(value: Int): ByteBuf = buf.writeInt(value)
    override fun writeIntLE(value: Int): ByteBuf = buf.writeIntLE(value)
    override fun writeLong(value: Long): ByteBuf = buf.writeLong(value)
    override fun writeLongLE(value: Long): ByteBuf = buf.writeLongLE(value)
    override fun writeMedium(value: Int): ByteBuf = buf.writeMedium(value)
    override fun writeMediumLE(value: Int): ByteBuf = buf.writeMediumLE(value)
    override fun writerIndex(): Int = buf.writerIndex()
    override fun writerIndex(writerIndex: Int): ByteBuf = buf.writerIndex(writerIndex)
    override fun writeShort(value: Int): ByteBuf = buf.writeShort(value)
    override fun writeShortLE(value: Int): ByteBuf = buf.writeShortLE(value)
    override fun writeZero(length: Int): ByteBuf = buf.writeZero(length)
    override fun refCnt(): Int = buf.refCnt()
    override fun release(): Boolean = buf.release()
    override fun release(decrement: Int): Boolean = buf.release(decrement)
}

