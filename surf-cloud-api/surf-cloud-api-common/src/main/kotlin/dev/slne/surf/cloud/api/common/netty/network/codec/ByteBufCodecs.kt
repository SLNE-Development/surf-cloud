package dev.slne.surf.cloud.api.common.netty.network.codec

import dev.slne.surf.cloud.api.common.netty.protocol.buffer.*
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.types.Utf8String
import dev.slne.surf.cloud.api.common.util.ByIdMap
import dev.slne.surf.cloud.api.common.util.ByIdMap.OutOfBoundsStrategy
import dev.slne.surf.cloud.api.common.util.Either
import dev.slne.surf.cloud.api.common.util.IdRepresentable
import dev.slne.surf.cloud.api.common.util.IdRepresentable.Companion.IdRepresentableCodecOperation
import dev.slne.surf.cloud.api.common.util.createUnresolvedInetSocketAddress
import dev.slne.surf.surfapi.core.api.util.objectListOf
import glm_.numberOfTrailingZeros
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufOutputStream
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.EncoderException
import it.unimi.dsi.fastutil.io.FastBufferedInputStream
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.BinaryTagIO
import net.kyori.adventure.nbt.BinaryTagType
import net.kyori.adventure.nbt.BinaryTagTypes
import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.SoundStop
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import org.spongepowered.math.vector.Vector3d
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

    val SOUND_EMITTER_SELF_CODEC = streamCodecUnitSimple(Sound.Emitter.self())

    private val SOUND_SOURCE_BY_ID = ByIdMap.continuous(
        { it.ordinal },
        Sound.Source.entries.toTypedArray(),
        OutOfBoundsStrategy.ZERO
    )

    val SOUND_SOURCE_CODEC = idMapper(SOUND_SOURCE_BY_ID) { it.ordinal }

    val SOUND_CODEC = streamCodecComposite(
        KEY_CODEC,
        Sound::name,
        SOUND_SOURCE_CODEC,
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

    val SOUND_STOP_CODEC = StreamCodec.composite(
        SOUND_SOURCE_CODEC.apply(::nullable),
        SoundStop::source,
        KEY_CODEC.apply(::nullable),
        SoundStop::sound
    ) { source, sound ->
        when {
            source == null && sound == null -> SoundStop.all()
            source == null && sound != null -> SoundStop.named(sound)
            source != null && sound == null -> SoundStop.source(source)
            source != null && sound != null -> SoundStop.namedOnSource(sound, source)
            else -> throw MatchException("Impossible state reached", null)
        }
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

    val JAVA_DURATION_CODEC = streamCodecComposite(
        LONG_CODEC,
        java.time.Duration::toMillis,
        java.time.Duration::ofMillis
    )

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

    private val TAG_TYPES = arrayOf(
        BinaryTagTypes.END,
        BinaryTagTypes.BYTE,
        BinaryTagTypes.SHORT,
        BinaryTagTypes.INT,
        BinaryTagTypes.LONG,
        BinaryTagTypes.FLOAT,
        BinaryTagTypes.DOUBLE,
        BinaryTagTypes.BYTE_ARRAY,
        BinaryTagTypes.STRING,
        BinaryTagTypes.LIST,
        BinaryTagTypes.COMPOUND,
        BinaryTagTypes.INT_ARRAY,
        BinaryTagTypes.LONG_ARRAY,
    )

    private val BINARY_TAG_BY_ID = ByIdMap.continuous(
        { it.id().toInt() },
        TAG_TYPES,
        OutOfBoundsStrategy.DECODE_ERROR
    )

    val BINARY_TAG_CODEC: StreamCodec<ByteBuf, BinaryTag> = streamCodec({ buf, tag ->
        val type = tag.type() as BinaryTagType<BinaryTag>
        buf.writeByte(type.id().toInt())

        val tmp = buf.alloc().buffer()
        try {
            DataOutputStream(FastBufferedOutputStream(ByteBufOutputStream(tmp))).use { out ->
                type.write(tag, out)
            }
            val length = tmp.readableBytes()
            buf.writeVarInt(length)
            buf.writeBytes(tmp, tmp.readerIndex(), length)
        } finally {
            tmp.release()
        }

    }, { buf ->
        val type = BINARY_TAG_BY_ID(buf.readByte().toInt())
        val len = buf.readVarInt()
        val slice = buf.readRetainedSlice(len)

        try {
            DataInputStream(FastBufferedInputStream(ByteBufInputStream(slice))).use { input ->
                type.read(input)
            }
        } finally {
            slice.release()
        }
    })

    val BINARY_TAG_CODEC_COMPRESSED: StreamCodec<ByteBuf, BinaryTag> = streamCodec({ buf, tag ->
        val type = tag.type() as BinaryTagType<BinaryTag>
        buf.writeByte(type.id().toInt())
        val temp = buf.alloc().buffer()

        try {
            DataOutputStream(FastBufferedOutputStream(GZIPOutputStream(ByteBufOutputStream(temp)))).use {
                type.write(tag, it)
            }
            buf.writeVarInt(temp.readableBytes())
            buf.writeBytes(temp, temp.readerIndex(), temp.readableBytes())
        } finally {
            temp.release()
        }
    }, { buf ->
        val type = BINARY_TAG_BY_ID(buf.readByte().toInt())
        val length = buf.readVarInt()
        val slice = buf.readRetainedSlice(length)

        try {
            DataInputStream(FastBufferedInputStream(GZIPInputStream(ByteBufInputStream(slice)))).use {
                type.read(it)
            }
        } finally {
            slice.release()
        }
    })

    val RESOURCE_PACK_INFO_CODEC = StreamCodec.composite(
        UUID_CODEC,
        ResourcePackInfo::id,
        URI_CODEC,
        ResourcePackInfo::uri,
        STRING_CODEC,
        ResourcePackInfo::hash,
        ResourcePackInfo::resourcePackInfo
    )

    val RESOURCE_PACK_REQUEST_CODEC = StreamCodec.composite(
        RESOURCE_PACK_INFO_CODEC.apply(list()),
        ResourcePackRequest::packs,
        BOOLEAN_CODEC,
        ResourcePackRequest::replace,
        BOOLEAN_CODEC,
        ResourcePackRequest::required,
        COMPONENT_CODEC.apply(this::nullable),
        ResourcePackRequest::prompt,
    ) { packs, replace, required, prompt ->
        ResourcePackRequest.resourcePackRequest()
            .packs(packs)
            .replace(replace)
            .required(required)
            .prompt(prompt)
            .build()
    }

    val TITLE_TIMES_CODEC = StreamCodec.composite(
        JAVA_DURATION_CODEC,
        Title.Times::fadeIn,
        JAVA_DURATION_CODEC,
        Title.Times::stay,
        JAVA_DURATION_CODEC,
        Title.Times::fadeOut,
        Title.Times::times
    )

    val TITLE_CODEC = StreamCodec.composite(
        COMPONENT_CODEC,
        Title::title,
        COMPONENT_CODEC,
        Title::subtitle,
        TITLE_TIMES_CODEC.apply(this::nullable),
        Title::times,
        Title::title
    )

    private val titleParts = objectListOf<TitlePart<*>>(
        TitlePart.TITLE,
        TitlePart.SUBTITLE,
        TitlePart.TIMES
    )

    val TITLE_PART_BY_ID = ByIdMap.continuous(
        titleParts::indexOf,
        titleParts.toTypedArray(),
        OutOfBoundsStrategy.DECODE_ERROR
    )

    val TITLE_PART_CODEC = idMapper(TITLE_PART_BY_ID, titleParts::indexOf)

    val BOSS_BAR_COLOR_CODEC = enumStreamCodec<BossBar.Color>()
    val BOSS_BAR_OVERLAY_CODEC = enumStreamCodec<BossBar.Overlay>()
    val BOSS_BAR_FLAGS_CODEC = enumStreamCodec<BossBar.Flag>()

    val BOSS_BAR_CODEC = StreamCodec.composite(
        COMPONENT_CODEC,
        BossBar::name,
        FLOAT_CODEC,
        BossBar::progress,
        BOSS_BAR_COLOR_CODEC,
        BossBar::color,
        BOSS_BAR_OVERLAY_CODEC,
        BossBar::overlay,
        BOSS_BAR_FLAGS_CODEC.apply(set(BossBar.Flag.entries.size)),
        BossBar::flags,
        BossBar::bossBar
    )

    val BOOK_CODEC = StreamCodec.composite(
        COMPONENT_CODEC,
        Book::title,
        COMPONENT_CODEC,
        Book::author,
        COMPONENT_CODEC.apply(list()),
        Book::pages,
        Book::book
    )

    val SPONGE_VECTOR_3D = StreamCodec.composite(
        DOUBLE_CODEC,
        Vector3d::x,
        DOUBLE_CODEC,
        Vector3d::y,
        DOUBLE_CODEC,
        Vector3d::z,
        ::Vector3d
    )

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

    fun <B : ByteBuf, L, R> either(
        leftCodec: StreamCodec<in B, L>,
        rightCodec: StreamCodec<in B, R>
    ): StreamCodec<B, Either<L, R>> {
        return object : StreamCodec<B, Either<L, R>> {
            override fun decode(buf: B): Either<L, R> {
                return if (buf.readBoolean()) Either.left(leftCodec.decode(buf)) else Either.right(
                    rightCodec.decode(buf)
                )
            }

            override fun encode(buf: B, value: Either<L, R>) {
                value.ifLeft { left ->
                    buf.writeBoolean(true)
                    leftCodec.encode(buf, left)
                }.ifRight { right ->
                    buf.writeBoolean(false)
                    rightCodec.encode(buf, right)
                }
            }
        }
    }

    fun <B : ByteBuf, F, S> pair(
        firstCodec: StreamCodec<in B, F>,
        secondCodec: StreamCodec<in B, S>,
    ): StreamCodec<B, Pair<F, S>> {
        return object : StreamCodec<B, Pair<F, S>> {
            override fun decode(buf: B): Pair<F, S> {
                val first = firstCodec.decode(buf)
                val second = secondCodec.decode(buf)
                return Pair(first, second)
            }

            override fun encode(buf: B, value: Pair<F, S>) {
                firstCodec.encode(buf, value.first)
                secondCodec.encode(buf, value.second)
            }
        }
    }

    fun <B : ByteBuf, V : Any> optional(codec: StreamCodec<in B, V>) =
        object : StreamCodec<B, Optional<V>> {
            override fun decode(buf: B): Optional<V> {
                return if (buf.readBoolean()) Optional.of(codec.decode(buf)) else Optional.empty()
            }

            override fun encode(buf: B, value: Optional<V>) {
                if (value.isPresent) {
                    buf.writeBoolean(true)
                    codec.encode(buf, value.get())
                } else {
                    buf.writeBoolean(false)
                }
            }
        }

    fun <B : ByteBuf, V : Any> nullable(codec: StreamCodec<in B, V>) =
        object : StreamCodec<B, V?> {
            override fun decode(buf: B): V? {
                return if (buf.readBoolean()) codec.decode(buf) else null
            }

            override fun encode(buf: B, value: V?) {
                if (value != null) {
                    buf.writeBoolean(true)
                    codec.encode(buf, value)
                } else {
                    buf.writeBoolean(false)
                }
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

    fun <B : ByteBuf, V> makeImmutableList(): CodecOperation<B, MutableList<V>, List<V>> {
        return CodecOperation { size ->
            size.map(
                { Collections.unmodifiableList(it) },
                ::ObjectArrayList
            )
        }
    }

    fun <B : ByteBuf, V> list(maxSize: Int): CodecOperation<B, V, MutableList<V>> {
        return CodecOperation { size -> collection(::ObjectArrayList, size, maxSize) }
    }

    fun <B : ByteBuf, V> set(): CodecOperation<B, V, MutableSet<V>> {
        return CodecOperation { size -> collection(::ObjectOpenHashSet, size) }
    }

    fun <B : ByteBuf, V> set(maxSize: Int): CodecOperation<B, V, MutableSet<V>> {
        return CodecOperation { size -> collection(::ObjectOpenHashSet, size, maxSize) }
    }

    inline fun <B : ByteBuf, reified V : Enum<V>> enumSet(): CodecOperation<B, V, EnumSet<V>> {
        return CodecOperation { size -> collection({ EnumSet.noneOf(V::class.java) }, size) }
    }

    /**
     * Creates and returns a codec operation for encoding and decoding an `EnumSet` of enums
     * that implement both the `Enum` and `IdRepresentable` interfaces.
     *
     * This codec uses the `id` property of the enums for efficient serialization
     * into compressed format, utilizing a variable-length encoding for storage of
     * the identifier bits in the `ByteBuf`.
     *
     * @return An instance of `IdRepresentableCodecOperation` for encoding and decoding
     *         an `EnumSet` of a specific type of enum implementing `IdRepresentable`.
     */
    inline fun <B : ByteBuf, reified V> idEnumSet(): IdRepresentableCodecOperation<B, V, EnumSet<V>> where V : Enum<V>, V : IdRepresentable {
        val constants = V::class.java.enumConstants
        val maxId = constants.maxOf { it.id }
        val longsNeeded = (maxId + 64) / 64

        return IdRepresentableCodecOperation { elementCodec ->
            object : StreamCodec<B, EnumSet<V>> {
                override fun decode(buf: B): EnumSet<V> {
                    val bits = LongArray(longsNeeded)
                    repeat(longsNeeded) {
                        bits[it] = buf.readVarLong()
                    }

                    val result = EnumSet.noneOf(V::class.java)
                    repeat(longsNeeded) { longIndex ->
                        var long = bits[longIndex]

                        while (long != 0L) {
                            val leastSignificantBit = long and -long
                            val bitIndex = long.numberOfTrailingZeros
                            val id = longIndex * 64 + bitIndex

                            if (id <= maxId) {
                                result.add(elementCodec.itemLookup(id))
                            }

                            long = long xor leastSignificantBit
                        }
                    }

                    return result
                }

                override fun encode(buf: B, value: EnumSet<V>) {
                    val bits = LongArray(longsNeeded)

                    for (v in value) {
                        val id = v.id
                        val idx = id / 64
                        val bit = id % 64
                        bits[idx] = bits[idx] or (1L shl bit)
                    }

                    repeat(longsNeeded) {
                        buf.writeVarLong(bits[it])
                    }
                }
            }
        }
    }


    inline fun <B : ByteBuf, reified V : Enum<V>> enumSet(maxSize: Int): CodecOperation<B, V, EnumSet<V>> {
        return CodecOperation { size ->
            collection(
                { EnumSet.noneOf(V::class.java) },
                size,
                maxSize
            )
        }
    }

    inline fun <B : ByteBuf, reified V : Any> array(maxSize: Int = Int.MAX_VALUE): CodecOperation<B, V, Array<V>> {
        return CodecOperation { elementCodec ->
            StreamCodec.ofMember<B, Array<V>>(
                { array, buf ->
                    val count = array.size
                    writeCount(buf, count, maxSize)
                    repeat(count) {
                        elementCodec.encode(buf, array[it])
                    }
                },
                { buf ->
                    val count = readCount(buf, maxSize)
                    val rawArray = arrayOfNulls<V>(count)

                    repeat(count) {
                        rawArray[it] = elementCodec.decode(buf)
                    }

                    rawArray.requireNoNulls()
                }
            )
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun decodeError(message: Any): Nothing =
        throw DecoderException(message.toString())

    @Suppress("NOTHING_TO_INLINE")
    private inline fun encodeError(message: Any): Nothing =
        throw EncoderException(message.toString())
}