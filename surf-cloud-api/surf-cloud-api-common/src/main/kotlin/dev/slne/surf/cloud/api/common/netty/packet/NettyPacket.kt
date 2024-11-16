package dev.slne.surf.cloud.api.common.netty.packet

import dev.slne.surf.cloud.api.common.meta.PacketCodec
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamDecoder
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamMemberEncoder
import dev.slne.surf.cloud.api.common.util.mutableObject2ObjectMapOf
import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.apache.commons.lang3.builder.ToStringBuilder
import org.jetbrains.annotations.ApiStatus.Internal
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.isAccessible
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.channels.Channel as CoroutineChannel


abstract class NettyPacket {

    internal var sessionId = ThreadLocalRandom.current().nextLong()
        private set
    private val meta = this::class.getPacketMeta()

    val id = meta.id
    val flow = meta.flow

    /**
     * Whether the packet is skippable or not.
     * If the packet is skippable, it will be ignored if the packet is too large.
     */
    open val skippable = false

    /**
     * Extra packets that should be sent after this packet.
     */
    open val extraPackets: List<NettyPacket>? = null

    @Internal
    open val terminal: Boolean = false

    /**
     * Custom logic to handle when the packet is too large.
     * Should return true if the method handled the packet, false otherwise.
     */
    open fun packetTooLarge(connection: Any): Boolean = false


    open fun isReady(): Boolean = true

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NettyPacket) return false

        if (id != other.id) return false
        if (flow != other.flow) return false
        if (skippable != other.skippable) return false
        if (extraPackets != other.extraPackets) return false
        if (terminal != other.terminal) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + flow.hashCode()
        result = 31 * result + skippable.hashCode()
        result = 31 * result + (extraPackets?.hashCode() ?: 0)
        result = 31 * result + terminal.hashCode()
        return result
    }

    override fun toString(): String = try {
        ToStringBuilder.reflectionToString(this).toString()
    } catch (e: Throwable) {
        "NettyPacket(id=$id, flow=$flow, skippable=$skippable, extraPackets=$extraPackets, terminal=$terminal)"
    }


    companion object {
        @JvmStatic
        fun <B : ByteBuf, T : NettyPacket> codec(
            encoder: StreamMemberEncoder<B, T>,
            decoder: StreamDecoder<B, T>
        ): StreamCodec<B, T> = StreamCodec.ofMember(encoder, decoder)
    }


    // TODO: 16.09.2024 14:49 - send method

    val channel: Channel
        get() = TODO()

    fun fireAndForget() {
    }

    suspend fun fire() = suspendCancellableCoroutine {
        channel.writeAndFlush(this)
            .also { future -> it.invokeOnCancellation { future.cancel(true) } }
            .addListener { future ->
                if (future.isSuccess) {
                    it.resume(Unit)
                } else {
                    it.resumeWithException(future.cause())
                }
            }
    }
}

abstract class RespondingNettyPacket<P : NettyPacket> : NettyPacket() {
    val requestId = UUID.randomUUID()

    suspend fun fireAndAwait(timeout: Duration = 15.seconds): P? = withTimeoutOrNull(timeout) {
        val responseChannel = CoroutineChannel<P>()

        // registerResponseChannel(requestId, responseChannel)

        try {
            fireAndForget()
            responseChannel.receive()
        } finally {
            // unregisterResponseChannel(requestId)
            responseChannel.close()
        }
    }

}

fun <B : ByteBuf, T : NettyPacket> packetCodec(
    encoder: StreamMemberEncoder<B, T>,
    decoder: StreamDecoder<B, T>
): StreamCodec<B, T> = NettyPacket.codec(encoder, decoder)

fun KClass<out NettyPacket>.getPacketMetaOrNull() = findAnnotation<SurfNettyPacket>()
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
