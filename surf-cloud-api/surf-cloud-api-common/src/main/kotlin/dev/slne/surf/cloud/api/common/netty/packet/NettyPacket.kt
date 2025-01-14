package dev.slne.surf.cloud.api.common.netty.packet

import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamDecoder
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamMemberEncoder
import io.netty.buffer.ByteBuf
import org.apache.commons.lang3.builder.ToStringBuilder
import org.jetbrains.annotations.ApiStatus.Internal
import java.util.*
import java.util.concurrent.ThreadLocalRandom


abstract class NettyPacket {

    // region Internal
    @Deprecated("internal use only")
    @Internal
    var handled = false
        private set

    @Deprecated("internal use only")
    @Internal
    @Suppress("DEPRECATION")
    fun handled() {
        handled = true
    }

    @Internal
    open val terminal: Boolean = false
    // endregion

    private val meta = this::class.getPacketMeta()
    val id = meta.id
    val flow = meta.flow
    val protocols = meta.protocols

    val sessionId = ThreadLocalRandom.current().nextLong()

    /**
     * Whether the packet is skippable or not.
     * If the packet is skippable, it will be ignored if the packet is too large.
     */
    open val skippable = false

    /**
     * Extra packets that should be sent after this packet.
     */
    open val extraPackets: List<NettyPacket>? = null

    /**
     * Custom logic to handle when the packet is too large.
     * Should return true if the method handled the packet, false otherwise.
     * No Exception should be thrown in this method.
     */
    open fun packetTooLarge(connection: Any): Boolean = false

    open fun isReady(): Boolean = true

    companion object {
        @JvmStatic
        fun <B : ByteBuf, T : NettyPacket> codec(
            encoder: StreamMemberEncoder<B, T>,
            decoder: StreamDecoder<B, T>
        ): StreamCodec<B, T> = StreamCodec.ofMember(encoder, decoder)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NettyPacket) return false

        if (sessionId != other.sessionId) return false
        if (id != other.id) return false
        if (flow != other.flow) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sessionId.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + flow.hashCode()
        return result
    }

    @Suppress("DEPRECATION")
    override fun toString(): String = runCatching {
        ToStringBuilder.reflectionToString(this).toString()
    }.getOrElse { "NettyPacket(id=$id, flow=$flow, skippable=$skippable, extraPackets=$extraPackets, terminal=$terminal, sessionId=$sessionId, handled=$handled)" }
}