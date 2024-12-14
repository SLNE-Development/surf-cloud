package dev.slne.surf.cloud.api.common.netty.packet

import dev.slne.surf.cloud.api.common.meta.PacketCodec
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamDecoder
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamMemberEncoder
import dev.slne.surf.cloud.api.common.util.mutableObject2ObjectMapOf
import io.netty.buffer.ByteBuf
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible

fun <B : ByteBuf, T : NettyPacket> packetCodec(
    encoder: StreamMemberEncoder<B, T>,
    decoder: StreamDecoder<B, T>
): StreamCodec<B, T> = NettyPacket.codec(encoder, decoder)


private val metaCache = mutableObject2ObjectMapOf<KClass<out NettyPacket>, SurfNettyPacket>(512)
fun KClass<out NettyPacket>.getPacketMetaOrNull() =
    metaCache[this] ?: findAnnotation<SurfNettyPacket>()?.also { metaCache[this] = it }

fun KClass<out NettyPacket>.getPacketMeta() = getPacketMetaOrNull()
    ?: error("NettyPacket class must be annotated with SurfNettyPacket")

fun Class<out NettyPacket>.getPacketMeta() = kotlin.getPacketMeta()

const val DEFAULT_STREAM_CODEC_NAME = "STREAM_CODEC"
private val codecCache = mutableObject2ObjectMapOf<KClass<out NettyPacket>, StreamCodec<*, *>>()

@Suppress("UNCHECKED_CAST")
fun <B : Any, V : Any> KClass<out NettyPacket>.findPacketCodec(): StreamCodec<in B, out V>? {
    codecCache[this]?.let { return it as? StreamCodec<in B, out V> }

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
