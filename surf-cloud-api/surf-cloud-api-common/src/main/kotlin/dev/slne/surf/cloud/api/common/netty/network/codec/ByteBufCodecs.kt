package dev.slne.surf.cloud.api.common.netty.network.codec

import dev.slne.surf.cloud.api.common.internal.BinaryTagTypeProxy
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.*
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.types.Utf8String
import dev.slne.surf.cloud.api.common.util.ByIdMap
import dev.slne.surf.cloud.api.common.util.ByIdMap.OutOfBoundsStrategy
import dev.slne.surf.cloud.api.common.util.createUnresolvedInetSocketAddress
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufOutputStream
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.EncoderException
import it.unimi.dsi.fastutil.io.FastBufferedInputStream
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.BinaryTagIO
import net.kyori.adventure.nbt.BinaryTagType
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.URI
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.math.min
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object ByteBufCodecs {
    const val MAX_INITIAL_COLLECTION_SIZE = 65536

    val BOOLEAN_CODEC = streamCodec(ByteBuf::writeBoolean, ByteBuf::readBoolean)
    val BYTE_CODEC = streamCodec({ buf, byte -> buf.writeByte(byte.toInt()) }, ByteBuf::readByte)
    val SHORT_CODEC =
        streamCodec({ buf, short -> buf.writeShort(short.toInt()) }, ByteBuf::readShort)

    val INT_CODEC = streamCodec(ByteBuf::writeInt, ByteBuf::readInt)
    val VAR_INT_CODEC = streamCodec(ByteBuf::writeVarInt, ByteBuf::readVarInt)
    val FLOAT_CODEC = streamCodec(ByteBuf::writeFloat, ByteBuf::readFloat)
    val DOUBLE_CODEC = streamCodec(ByteBuf::writeDouble, ByteBuf::readDouble)
    val LONG_CODEC = streamCodec(ByteBuf::writeLong, ByteBuf::readLong)
    val VAR_LONG_CODEC = streamCodec(ByteBuf::writeVarLong, ByteBuf::readVarLong)
    val STRING_CODEC = Utf8String.STREAM_CODEC


    val UUID_CODEC = streamCodec(ByteBuf::writeUuid, ByteBuf::readUuid)
    val BYTE_ARRAY_CODEC = streamCodec(ByteBuf::writeByteArray, ByteBuf::readByteArray)

    val OPTIONAL_LONG_CODEC = streamCodec<ByteBuf, OptionalLong>({ buf, optionalLong ->
        BOOLEAN_CODEC.encode(buf, optionalLong.isPresent)
        optionalLong.ifPresent { LONG_CODEC.encode(buf, it) }
    }) { buf ->
        if (BOOLEAN_CODEC.decode(buf)) {
            OptionalLong.of(LONG_CODEC.decode(buf))
        } else {
            OptionalLong.empty()
        }
    }

    val KEY_CODEC = streamCodecComposite(STRING_CODEC, Key::asString, Key::key)


    private val SOUND_SOURCE_BY_ID = ByIdMap.continuous(
        { it.ordinal },
        Sound.Source.entries.toTypedArray(),
        OutOfBoundsStrategy.ZERO
    )
    val SOUND_CODEC = streamCodecComposite(
        KEY_CODEC,
        Sound::name,
        idMapper(SOUND_SOURCE_BY_ID) { it.ordinal },
        { it.source() },
        FLOAT_CODEC,
        Sound::volume,
        FLOAT_CODEC,
        Sound::pitch,
        OPTIONAL_LONG_CODEC,
        Sound::seed
    ) { type, source, volume, pitch, seed ->
        Sound.sound()
            .type(type)
            .source(source)
            .volume(volume)
            .pitch(pitch)
            .seed(seed)
            .build()
    }

    val COMPONENT_CODEC = streamCodecComposite(
        STRING_CODEC,
        { GsonComponentSerializer.gson().serialize(it.compact()) },
        { GsonComponentSerializer.gson().deserialize(it) }
    )

    val COMPOUND_TAG_CODEC = streamCodecComposite(BYTE_ARRAY_CODEC, { tag ->
        ByteArrayOutputStream().use { out ->
            BinaryTagIO.writer().write(tag, out, BinaryTagIO.Compression.GZIP)
            out.toByteArray()
        }
    }, { bytes ->
        ByteArrayInputStream(bytes).use { input ->
            BinaryTagIO.unlimitedReader().read(input, BinaryTagIO.Compression.GZIP)
        }
    })

    val URI_CODEC = streamCodecComposite(STRING_CODEC, URI::toString, URI::create)
    val INET_SOCKET_ADDRESS_CODEC = streamCodecComposite(
        STRING_CODEC,
        InetSocketAddress::getHostString,
        INT_CODEC,
        InetSocketAddress::getPort,
        ::createUnresolvedInetSocketAddress
    )
    val INET_4_ADDRESS_CODEC = streamCodecComposite(
        BYTE_ARRAY_CODEC,
        Inet4Address::getAddress
    ) { Inet4Address.getByAddress(it) as Inet4Address }

    val ZONED_DATE_TIME_CODEC = streamCodecComposite(
        LONG_CODEC,
        { it.toInstant().toEpochMilli() },
        STRING_CODEC,
        { it.zone.id },
        { epoch, zoneId -> ZonedDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneId.of(zoneId)) }
    )

    val DURATION_CODEC = streamCodecComposite(
        LONG_CODEC,
        Duration::inWholeMilliseconds
    ) { it.milliseconds }

    val BIG_INTEGER_CODEC =
        streamCodecComposite(BYTE_ARRAY_CODEC, BigInteger::toByteArray, ::BigInteger)
    val BIG_DECIMAL_CODEC = streamCodecComposite(
        BIG_INTEGER_CODEC,
        BigDecimal::unscaledValue,
        VAR_INT_CODEC,
        BigDecimal::scale,
        VAR_INT_CODEC,
        BigDecimal::precision
    ) { unscaledValue, scale, precision ->
        BigDecimal(unscaledValue, scale, MathContext(precision))
    }


    private val BINARY_TAG_BY_ID = ByIdMap.continuous(
        { it.id().toInt() },
        BinaryTagTypeProxy.instance.getTypes().toTypedArray(),
        OutOfBoundsStrategy.DECODE_ERROR
    )

    val BINARY_TAG_CODEC: StreamCodec<ByteBuf, BinaryTag> = streamCodec({ buf, tag ->
        val type = tag.type() as BinaryTagType<BinaryTag>
        buf.writeByte(type.id().toInt())
        DataOutputStream(FastBufferedOutputStream(ByteBufOutputStream(buf))).use {
            type.write(tag, it)
        }
    }, { buf ->
        val type = BINARY_TAG_BY_ID(buf.readByte().toInt())
        DataInputStream(FastBufferedInputStream(ByteBufInputStream(buf))).use {
            type.read(it)
        }
    })

    val BINARY_TAG_CODEC_COMPRESSED: StreamCodec<ByteBuf, BinaryTag> = streamCodec({ buf, tag ->
        val type = tag.type() as BinaryTagType<BinaryTag>
        buf.writeByte(type.id().toInt())
        DataOutputStream(FastBufferedOutputStream(GZIPOutputStream(ByteBufOutputStream(buf)))).use {
            type.write(tag, it)
        }
    }, { buf ->
        val type = BINARY_TAG_BY_ID(buf.readByte().toInt())
        DataInputStream(FastBufferedInputStream(GZIPInputStream(ByteBufInputStream(buf)))).use {
            type.read(it)
        }
    })

    fun <E : Enum<E>> enumStreamCodec(clazz: Class<E>): StreamCodec<ByteBuf, E> =
        streamCodec(ByteBuf::writeEnum) { it.readEnum(clazz) }

    inline fun <reified E : Enum<E>> enumStreamCodec(): StreamCodec<ByteBuf, E> =
        streamCodec(ByteBuf::writeEnum) { it.readEnum<E>() }


    fun <T> idMapper(
        idLookup: (Int) -> T,
        idGetter: (T) -> Int
    ): StreamCodec<ByteBuf, T> = object : StreamCodec<ByteBuf, T> {
        override fun decode(buf: ByteBuf): T = idLookup(buf.readVarInt())
        override fun encode(buf: ByteBuf, value: T) {
            buf.writeVarInt(idGetter(value))
        }
    }

    fun readCount(buffer: ByteBuf, maxSize: Int): Int {
        val count = buffer.readVarInt()
        if (count > maxSize) {
            throw DecoderException("$count elements exceeded max size of: $maxSize")
        } else {
            return count
        }
    }

    fun writeCount(buffer: ByteBuf, count: Int, maxSize: Int) {
        if (count > maxSize) {
            throw EncoderException("$count elements exceeded max size of: $maxSize")
        } else {
            buffer.writeVarInt(count)
        }
    }

    fun <B : ByteBuf, V, C : MutableCollection<V>> collection(
        factory: (Int) -> C,
        codec: StreamCodec<in B, V>,
        maxSize: Int = Int.MAX_VALUE
    ): StreamCodec<B, C> = object : StreamCodec<B, C> {
        override fun decode(buf: B): C {
            val count = readCount(buf, maxSize)
            val collection = factory(min(count, MAX_INITIAL_COLLECTION_SIZE))

            repeat(count) {
                collection.add(codec.decode(buf))
            }

            return collection
        }

        override fun encode(buf: B, value: C) {
            writeCount(buf, value.size, maxSize)

            for (element in value) {
                codec.encode(buf, element)
            }
        }
    }

    fun <B : ByteBuf, V, C : MutableCollection<V>> collection(factory: (Int) -> C): CodecOperation<B, V, C> {
        return CodecOperation { size -> collection(factory, size) }
    }


    fun <B : ByteBuf, V> list(): CodecOperation<B, V, MutableList<V>> {
        return CodecOperation { size -> collection(::ObjectArrayList, size) }
    }

    fun <B : ByteBuf, V> list(maxSize: Int): CodecOperation<B, V, MutableList<V>> {
        return CodecOperation { size -> collection(::ObjectArrayList, size, maxSize) }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun decodeError(message: Any): Nothing =
        throw DecoderException(message.toString())

    @Suppress("NOTHING_TO_INLINE")
    private inline fun encodeError(message: Any): Nothing =
        throw EncoderException(message.toString())
}