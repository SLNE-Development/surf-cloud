@file:OptIn(ExperimentalContracts::class, ExperimentalStdlibApi::class)

package dev.slne.surf.cloud.api.common.util.codec

import com.mojang.datafixers.kinds.App
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.*
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecUnitSimple
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.*
import dev.slne.surf.cloud.api.common.util.toIntArray
import dev.slne.surf.cloud.api.common.util.toUuid
import dev.slne.surf.surfapi.core.api.util.objectListOf
import io.netty.buffer.ByteBuf
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.BinaryTagIO
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.resource.ResourcePackInfo
import net.kyori.adventure.resource.ResourcePackRequest
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.SoundStop
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.title.Title
import net.kyori.adventure.title.TitlePart
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Duration
import java.util.*
import java.util.function.Function
import java.util.function.IntFunction
import java.util.function.ToIntFunction
import java.util.stream.Stream
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.enums.EnumEntries
import kotlin.enums.enumEntries

@DslMarker
annotation class RecordCodecDsl

fun <T> createRecordCodec(@RecordCodecDsl block: RecordCodecBuilder.Instance<T>.() -> App<RecordCodecBuilder.Mu<T>, T>): Codec<T> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return RecordCodecBuilder.create(block)
}

fun <T> createRecordMapCodec(@RecordCodecDsl block: RecordCodecBuilder.Instance<T>.() -> App<RecordCodecBuilder.Mu<T>, T>): MapCodec<T> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return RecordCodecBuilder.mapCodec(block)
}

fun <A, O> MapCodec<A>.getter(getter: O.() -> A): RecordCodecBuilder<O, A> =
    this.forGetter(Function(getter))

@Suppress("UnusedReceiverParameter")
fun <T> RecordCodecBuilder.Instance<T>.int(fieldName: String, getter: T.() -> Int) =
    Codec.INT.fieldOf(fieldName).getter(getter)

@Suppress("UnusedReceiverParameter")
fun <T> RecordCodecBuilder.Instance<T>.bool(fieldName: String, getter: T.() -> Boolean) =
    Codec.BOOL.fieldOf(fieldName).getter(getter)

@Suppress("UnusedReceiverParameter")
fun <T> RecordCodecBuilder.Instance<T>.string(fieldName: String, getter: T.() -> String) =
    Codec.STRING.fieldOf(fieldName).getter(getter)

@Suppress("UnusedReceiverParameter")
fun <T> RecordCodecBuilder.Instance<T>.stringOptionalLenient(
    fieldName: String,
    getter: T.() -> String?
) =
    Codec.STRING.lenientOptionalFieldOf(fieldName).forGetter<T> { Optional.ofNullable(getter(it)) }

@Suppress("UnusedReceiverParameter")
fun <T> RecordCodecBuilder.Instance<T>.float(fieldName: String, getter: T.() -> Float) =
    Codec.FLOAT.fieldOf(fieldName).getter(getter)

@Suppress("UnusedReceiverParameter")
fun <T> RecordCodecBuilder.Instance<T>.double(fieldName: String, getter: T.() -> Double) =
    Codec.DOUBLE.fieldOf(fieldName).getter(getter)

@Suppress("UnusedReceiverParameter")
fun <T> RecordCodecBuilder.Instance<T>.long(fieldName: String, getter: T.() -> Long) =
    Codec.LONG.fieldOf(fieldName).getter(getter)

@Suppress("UnusedReceiverParameter")
fun <T> RecordCodecBuilder.Instance<T>.byte(fieldName: String, getter: T.() -> Byte) =
    Codec.BYTE.fieldOf(fieldName).getter(getter)

@Suppress("UnusedReceiverParameter")
fun <T> RecordCodecBuilder.Instance<T>.short(fieldName: String, getter: T.() -> Short) =
    Codec.SHORT.fieldOf(fieldName).getter(getter)

@Suppress("UnusedReceiverParameter")
inline fun <T, reified E : Enum<E>> RecordCodecBuilder.Instance<T>.enum(
    fieldName: String,
    noinline getter: T.() -> E
) =
    ExtraCodecs.enumCodec(enumEntries<E>()).fieldOf(fieldName).getter(getter)

object ExtraCodecs {
    val JAVA = converter(JavaOps.INSTANCE)
    val UUID_CODEC: Codec<UUID> = Codec.INT_STREAM.comapFlatMap(
        { uuidStream -> fixedSize(uuidStream, 4).map { it.toUuid() } },
        { Arrays.stream(it.toIntArray()) }
    )
    val UUID_STRING_CODEC: Codec<UUID> = Codec.STRING.comapFlatMap(
        {
            runCatching { UUID.fromString(it) }.fold(
                { DataResult.success(it) },
                { DataResult.error { "Invalid UUID: $it" } })
        },
        { it.toString() })
    val UUID_LENIENT_CODEC = Codec.withAlternative(UUID_CODEC, UUID_STRING_CODEC)

    fun <T> converter(ops: DynamicOps<T>): Codec<T> =
        Codec.PASSTHROUGH.xmap({ it.convert(ops).value }, { Dynamic(ops, it as T) })


    fun <T : Enum<T>> enumCodec(entries: EnumEntries<T>) = Codec.STRING.validate { input ->
        if (entries.any { it.name == input })
            DataResult.success(input)
        else DataResult.error { "Invalid enum value: $input" }
    }.xmap({ input -> entries.first { it.name == input } }, { it.name })

    fun <E> idResolverCodec(
        elementToRawId: ToIntFunction<E?>,
        rawIdToElement: IntFunction<E?>,
        errorRawId: Int
    ): Codec<E> = Codec.INT
        .flatXmap(
            { rawId ->
                Optional.ofNullable(rawIdToElement.apply(rawId))
                    .map { DataResult.success(it) }
                    .orElseGet { DataResult.error { "Unknown element id: $rawId" } }
            },
            { element ->
                val rawId = elementToRawId.applyAsInt(element as E)
                if (rawId == errorRawId) DataResult.error { "Element with unknown id: $element" }
                else DataResult.success(rawId)
            }
        )


    fun <E> orCompressed(uncompressedCodec: Codec<E>, compressedCodec: Codec<E>): Codec<E> {
        return object : Codec<E> {
            override fun <T> encode(
                obj: E,
                dynamicOps: DynamicOps<T>,
                obj2: T
            ): DataResult<T> = if (dynamicOps.compressMaps()) compressedCodec.encode(
                obj,
                dynamicOps,
                obj2
            ) else uncompressedCodec.encode(obj, dynamicOps, obj2)


            override fun <T> decode(
                dynamicOps: DynamicOps<T>,
                obj: T
            ): DataResult<Pair<E, T>> = if (dynamicOps.compressMaps()) compressedCodec.decode(
                dynamicOps,
                obj
            ) else uncompressedCodec.decode(dynamicOps, obj)


            override fun toString(): String {
                return "$uncompressedCodec orCompressed $compressedCodec"
            }
        }
    }

    fun <E> orCompressed(
        uncompressedCodec: MapCodec<E>,
        compressedCodec: MapCodec<E>
    ): MapCodec<E> {
        return object : MapCodec<E>() {
            override fun <T> encode(
                obj: E,
                dynamicOps: DynamicOps<T>,
                recordBuilder: RecordBuilder<T>
            ): RecordBuilder<T> = if (dynamicOps.compressMaps()) compressedCodec.encode(
                obj,
                dynamicOps,
                recordBuilder
            ) else uncompressedCodec.encode(obj, dynamicOps, recordBuilder)


            override fun <T> decode(dynamicOps: DynamicOps<T>, mapLike: MapLike<T>): DataResult<E> =
                if (dynamicOps.compressMaps()) compressedCodec.decode(
                    dynamicOps,
                    mapLike
                ) else uncompressedCodec.decode(dynamicOps, mapLike)


            override fun <T> keys(dynamicOps: DynamicOps<T>): Stream<T> {
                return compressedCodec.keys(dynamicOps)
            }

            override fun toString(): String {
                return "$uncompressedCodec orCompressed $compressedCodec"
            }
        }
    }

    fun <T> nonEmptyList(originalCodec: Codec<List<T>>): Codec<List<T>> = originalCodec.validate {
        if (it.isEmpty()) DataResult.error { "List must have contents" } else DataResult.success(it)
    }

    val DURATION_STREAM_CODEC = streamCodec<ByteBuf, Duration>({ buf, duration ->
        buf.writeLong(duration.toMillis())
    }, { buf ->
        Duration.ofMillis(buf.readLong())
    })

    // region adventure
    val COMPONENT = Codec.STRING.comapFlatMap(
        {
            runCatching { GsonComponentSerializer.gson().deserialize(it) }.fold(
                { DataResult.success(it) },
                { DataResult.error { "Invalid component: $it" } })
        },
        { GsonComponentSerializer.gson().serialize(it) }
    )

    val COMPONENT_STREAM = streamCodec<ByteBuf, Component>({ buf, comp ->
        val str = GsonComponentSerializer.gson().serialize(comp)
        buf.writeUtf(str)
    }, { buf ->
        val str = buf.readUtf()
        GsonComponentSerializer.gson().deserialize(str)
    })

    val TITLE_TIMES_STREAM_CODEC = streamCodec<ByteBuf, Title.Times>({ buf, times ->
        DURATION_STREAM_CODEC.encode(buf, times.fadeIn())
        DURATION_STREAM_CODEC.encode(buf, times.stay())
        DURATION_STREAM_CODEC.encode(buf, times.fadeOut())
    }, { buf ->
        Title.Times.times(
            DURATION_STREAM_CODEC.decode(buf),
            DURATION_STREAM_CODEC.decode(buf),
            DURATION_STREAM_CODEC.decode(buf)
        )
    })

    val STREAM_TITLE_CODEC = streamCodec<SurfByteBuf, Title>({ buf, title ->
        COMPONENT_STREAM.encode(buf, title.title())
        COMPONENT_STREAM.encode(buf, title.subtitle())
        buf.writeNullable(title.times(), TITLE_TIMES_STREAM_CODEC::encode)
    }, { buf ->
        Title.title(
            COMPONENT_STREAM.decode(buf),
            COMPONENT_STREAM.decode(buf),
            buf.readNullable(TITLE_TIMES_STREAM_CODEC::decode)
        )
    })

    private val titlePartIndex = objectListOf(
        TitlePart.TITLE,
        TitlePart.SUBTITLE,
        TitlePart.TIMES
    )
    val STREAM_TITLE_PART_CODEC = streamCodec<SurfByteBuf, TitlePart<*>>({ buf, part ->
        buf.writeVarInt(titlePartIndex.indexOf(part))
    }, { buf ->
        titlePartIndex[buf.readVarInt()]
    })

    val STREAM_BOSSBAR_CODEC = streamCodec<ByteBuf, BossBar>({ buf, bar ->
        COMPONENT_STREAM.encode(buf, bar.name())
        buf.writeFloat(bar.progress())
        buf.writeEnum(bar.color())
        buf.writeEnum(bar.overlay())
        buf.writeCollection(bar.flags(), SurfByteBuf::writeEnum)
    }, { buf ->
        BossBar.bossBar(
            COMPONENT_STREAM.decode(buf),
            buf.readFloat(),
            buf.readEnum(),
            buf.readEnum(),
            buf.readCollection(::ObjectOpenHashSet) { buf.readEnum() }
        )
    })

    val STREAM_SOUND_CODEC = streamCodec<ByteBuf, Sound>({ buf, sound ->
        buf.writeEnum(sound.source())
        buf.writeFloat(sound.volume())
        buf.writeFloat(sound.pitch())
        buf.writeOptionalLong(sound.seed())
        buf.writeKey(sound.name())
    }, { buf ->
        Sound.sound()
            .source(buf.readEnum<Sound.Source>())
            .volume(buf.readFloat())
            .pitch(buf.readFloat())
            .seed(buf.readOptionalLong())
            .type(buf.readKey())
            .build()
    })

    val STREAM_EMITTER_SELF_CODEC = streamCodecUnitSimple(Sound.Emitter.self())

    val STREAM_SOUND_STOP_CODEC = streamCodec<ByteBuf, SoundStop>({ buf, stop ->
        buf.writeNullable(stop.source(), SurfByteBuf::writeEnum)
        buf.writeNullable(stop.sound(), SurfByteBuf::writeKey)
    }, { buf ->
        val source = buf.readNullable { it.readEnum<Sound.Source>() }
        val sound = buf.readNullable { it.readKey() }

        when {
            source == null && sound == null -> SoundStop.all()
            source == null && sound != null -> SoundStop.named(sound)
            source != null && sound == null -> SoundStop.source(source)
            source != null && sound != null -> SoundStop.namedOnSource(sound, source)
            else -> error("Impossible")
        }
    })

    val STREAM_BOOK_CODEC = streamCodec<ByteBuf, Book>({ buf, book ->
        COMPONENT_STREAM.encode(buf, book.title())
        COMPONENT_STREAM.encode(buf, book.author())
        buf.writeCollection(book.pages(), COMPONENT_STREAM::encode)
    }, { buf ->
        Book.book(
            COMPONENT_STREAM.decode(buf),
            COMPONENT_STREAM.decode(buf),
            buf.readList(COMPONENT_STREAM::decode)
        )
    })

    val STREAM_RESOURCE_PACK_INFO = streamCodec<ByteBuf, ResourcePackInfo>({ buf, info ->
        buf.writeUuid(info.id())
        buf.writeURI(info.uri())
        buf.writeUtf(info.hash())
    }, { buf ->
        ResourcePackInfo.resourcePackInfo(
            buf.readUuid(),
            buf.readURI(),
            buf.readUtf()
        )
    })

    val STREAM_RESOURCE_PACK_REQUEST_CODEC =
        streamCodec<ByteBuf, ResourcePackRequest>({ buf, request ->
            buf.writeCollection(request.packs(), STREAM_RESOURCE_PACK_INFO::encode)
            buf.writeBoolean(request.replace())
            buf.writeBoolean(request.required())
            buf.writeNullable(request.prompt(), COMPONENT_STREAM::encode)
        }, { buf ->
            ResourcePackRequest.resourcePackRequest()
                .packs(buf.readList(STREAM_RESOURCE_PACK_INFO::decode))
                .replace(buf.readBoolean())
                .required(buf.readBoolean())
                .prompt(buf.readNullable(COMPONENT_STREAM::decode))
                .build()
        })


    val KEY_CODEC: Codec<Key> =
        Codec.STRING.comapFlatMap(
            { if (Key.parseable(it)) DataResult.success(Key.key(it)) else DataResult.error { "Cannot convert $it to adventure Key" } },
            { it.asString() })

    val STREAM_KEY_CODEC = streamCodec<ByteBuf, Key>({ buf, key ->
        buf.writeUtf(key.asString())
    }, { buf ->
        Key.key(buf.readUtf())
    })

    // endregion
    // region nbt
    val COMPOUND_TAG_CODEC = streamCodec<ByteBuf, CompoundBinaryTag>({ buf, tag ->
        ByteArrayOutputStream().use { out ->
            BinaryTagIO.writer().write(tag, out, BinaryTagIO.Compression.GZIP)
            buf.writeByteArray(out.toByteArray())
        }
    }, { buf ->
        val bytes = buf.readByteArray()
        ByteArrayInputStream(bytes).use { input ->
            BinaryTagIO.unlimitedReader().read(input, BinaryTagIO.Compression.GZIP)
        }
    })
    // endregion

}

fun tryCollapseToString(component: Component): String? {
    if (component is TextComponent) {
        if (component.children().isEmpty() && component.style().isEmpty) {
            return component.content()
        }
    }
    return null
}

class FuzzyCodec<T>(
    private val codecs: List<MapCodec<out T>>,
    private val encoderGetter: (T) -> MapEncoder<out T>
) : MapCodec<T>() {

    @Suppress("UNCHECKED_CAST")
    override fun <S> decode(dynamicOps: DynamicOps<S>, mapLike: MapLike<S>): DataResult<T> {
        for (mapDecoder in this.codecs) {
            val dataResult = mapDecoder.decode(dynamicOps, mapLike)
            if (dataResult.result().isPresent) {
                return dataResult as DataResult<T>
            }
        }

        return DataResult.error { "No matching codec found" }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <S> encode(
        obj: T,
        dynamicOps: DynamicOps<S>,
        recordBuilder: RecordBuilder<S>
    ): RecordBuilder<S> {
        val mapEncoder = encoderGetter(obj) as MapEncoder<T>
        return mapEncoder.encode(obj, dynamicOps, recordBuilder)
    }

    override fun <S> keys(dynamicOps: DynamicOps<S>): Stream<S> = codecs.stream()
        .flatMap { it.keys(dynamicOps) }
        .distinct()


    override fun toString(): String {
        return "FuzzyCodec[" + this.codecs + "]"
    }
}

class StrictEither<T>(
    private val typeFieldName: String,
    private val typed: MapCodec<T>,
    private val fuzzy: MapCodec<T>
) : MapCodec<T>() {
    override fun <O> decode(dynamicOps: DynamicOps<O>, mapLike: MapLike<O>): DataResult<T> {
        return if (mapLike[typeFieldName] != null) typed.decode(
            dynamicOps,
            mapLike
        ) else fuzzy.decode(dynamicOps, mapLike)
    }

    override fun <O> encode(
        obj: T,
        dynamicOps: DynamicOps<O>,
        recordBuilder: RecordBuilder<O>
    ): RecordBuilder<O> {
        return fuzzy.encode(obj, dynamicOps, recordBuilder)
    }

    override fun <T1> keys(dynamicOps: DynamicOps<T1>): Stream<T1> {
        return Stream.concat(
            typed.keys(dynamicOps),
            fuzzy.keys(dynamicOps)
        ).distinct()
    }
}