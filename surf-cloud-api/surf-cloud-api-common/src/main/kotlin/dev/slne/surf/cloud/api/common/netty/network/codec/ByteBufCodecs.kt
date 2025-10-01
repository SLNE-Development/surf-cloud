package dev.slne.surf.cloud.api.common.netty.network.codec

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.*
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.types.Utf8String
import dev.slne.surf.cloud.api.common.util.ByIdMap
import dev.slne.surf.cloud.api.common.util.createUnresolvedInetSocketAddress
import io.netty.buffer.ByteBuf
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.BinaryTagIO
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object ByteBufCodecs {
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
        ByIdMap.OutOfBoundsStrategy.ZERO
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
}