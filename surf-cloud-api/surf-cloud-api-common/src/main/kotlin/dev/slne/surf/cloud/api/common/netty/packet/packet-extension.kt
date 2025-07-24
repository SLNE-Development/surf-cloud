package dev.slne.surf.cloud.api.common.netty.packet

import dev.slne.surf.cloud.api.common.meta.PacketCodec
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamDecoder
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamMemberEncoder
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import io.netty.buffer.ByteBuf
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import kotlinx.serialization.serializerOrNull
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible

/**
 * Creates a [StreamCodec] for encoding and decoding Netty packets.
 *
 * @param encoder The encoder for the packet type.
 * @param decoder The decoder for the packet type.
 * @return A [StreamCodec] that handles the specified packet type.
 */
fun <B : ByteBuf, T : NettyPacket> packetCodec(
    encoder: StreamMemberEncoder<B, T>,
    decoder: StreamDecoder<B, T>
): StreamCodec<B, T> = NettyPacket.codec(encoder, decoder)

// Internal cache for packet metadata and codecs.
private val metaCache = mutableObject2ObjectMapOf<KClass<out NettyPacket>, SurfNettyPacket>(512)

/**
 * Retrieves the metadata for a [NettyPacket] class if available.
 *
 * @return The [SurfNettyPacket] metadata annotation or `null` if not found.
 */
fun KClass<out NettyPacket>.getPacketMetaOrNull() =
    metaCache[this] ?: findAnnotation<SurfNettyPacket>()?.also { metaCache[this] = it }

/**
 * Retrieves the metadata for a [NettyPacket] class.
 *
 * @throws IllegalStateException If the [SurfNettyPacket] annotation is missing.
 * @return The [SurfNettyPacket] metadata annotation.
 */
fun KClass<out NettyPacket>.getPacketMeta() = getPacketMetaOrNull()
    ?: error("NettyPacket class '$qualifiedName' must be annotated with @${SurfNettyPacket::class.simpleName}")

/**
 * Retrieves the metadata for a [NettyPacket] class from a [Class] reference.
 *
 * @throws IllegalStateException If the [SurfNettyPacket] annotation is missing.
 * @return The [SurfNettyPacket] metadata annotation.
 */
fun Class<out NettyPacket>.getPacketMeta() = kotlin.getPacketMeta()

// Default property name for locating stream codecs in packet classes.
private const val DEFAULT_STREAM_CODEC_NAME = "STREAM_CODEC"

// Internal cache for codecs associated with packet classes.
private val codecCache = mutableObject2ObjectMapOf<KClass<out NettyPacket>, StreamCodec<*, *>>()


@OptIn(InternalSerializationApi::class)
fun <P : Any> KClass<P>.createCodec(): StreamCodec<SurfByteBuf, P> {
    return SurfByteBuf.streamCodecFromKotlin(serializer())
}

/**
 * Finds a [StreamCodec] for the specified packet type if available.
 *
 * The function searches for a codec property in the class or its companion object,
 * looking for annotations like [PacketCodec] or default property names.
 *
 * @return The [StreamCodec] for the packet type, or `null` if not found.
 */
@OptIn(InternalSerializationApi::class)
@Suppress("UNCHECKED_CAST")
fun <B : ByteBuf, V : NettyPacket> KClass<out V>.findPacketCodec(): StreamCodec<in B, out V>? {
    codecCache[this]?.let { return it as? StreamCodec<in B, out V> }

    val serializer = serializerOrNull()
    if (serializer != null) {
        return SurfByteBuf.streamCodecFromKotlin(serializer)
    }

    val properties =
        declaredMemberProperties + (companionObject?.declaredMemberProperties ?: emptyList())

    val codecProperty = properties.find {
        it.findAnnotation<PacketCodec>() != null ||
                (it.name == DEFAULT_STREAM_CODEC_NAME && it.returnType.classifier == StreamCodec::class)
    }?.apply { isAccessible = true }

    val codec = codecProperty?.let { prop ->
        when {
            objectInstance != null -> prop.call(objectInstance)
            companionObjectInstance != null -> prop.call(companionObjectInstance)
            else -> prop.call(null)
        } as? StreamCodec<in B, out V>
    }

    codec?.let { codecCache[this] = it }

    return codec
}