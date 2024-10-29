package dev.slne.surf.cloud.api.netty.packet

import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.netty.network.codec.StreamDecoder
import dev.slne.surf.cloud.api.netty.network.codec.StreamMemberEncoder
import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow
import io.netty.buffer.ByteBuf
import org.jetbrains.annotations.ApiStatus.Internal
import java.util.concurrent.ThreadLocalRandom
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation


abstract class NettyPacket {

    internal var sessionId = ThreadLocalRandom.current().nextLong()
        private set
    val id: Int
    val flow: PacketFlow

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

    init {
        val meta = this::class.getPacketMeta()
        this.id = meta.id
        this.flow = meta.flow
    }

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

    override fun toString(): String {
        return "NettyPacket(extraPackets=$extraPackets, sessionId=$sessionId, id=$id, flow=$flow, skippable=$skippable, terminal=$terminal)"
    }

    companion object {
        @JvmStatic
        fun <B : ByteBuf, T : NettyPacket> codec(
            encoder: StreamMemberEncoder<B, T>,
            decoder: StreamDecoder<B, T>
        ): StreamCodec<B, T> = StreamCodec.ofMember(encoder, decoder)
    }


    // TODO: 16.09.2024 14:49 - send method
}

fun KClass<out NettyPacket>.getPacketMeta() = findAnnotation<SurfNettyPacket>()
    ?: error("NettyPacket class must be annotated with SurfNettyPacket")

fun Class<out NettyPacket>.getPacketMeta() = kotlin.getPacketMeta()

fun <B: ByteBuf, T: NettyPacket> packetCodec(
    encoder: StreamMemberEncoder<B, T>,
    decoder: StreamDecoder<B, T>
): StreamCodec<B, T> = NettyPacket.codec(encoder, decoder)