package dev.slne.surf.cloud.api.common.netty.packet

import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamDecoder
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamMemberEncoder
import dev.slne.surf.cloud.api.common.util.InternalApi
import io.netty.buffer.ByteBuf
import org.apache.commons.lang3.builder.ToStringBuilder
import java.util.concurrent.ThreadLocalRandom


abstract class NettyPacket {

    // region Internal
    /**
     * Indicates whether this packet has been handled.
     * This is for internal use and is not intended to be modified externally.
     */
    @InternalApi
    var handled = false
        private set

    /**
     * Marks the packet as handled. For internal use only.
     */
    @InternalApi
    fun handled() {
        handled = true
    }

    /**
     * Indicates whether the packet is terminal, meaning no further processing
     * is required after this packet and the protocol switches to the next state.
     */
    @InternalApi
    open val terminal: Boolean = false
    // endregion

    private val meta = this::class.getPacketMeta()

    /**
     * The unique identifier of this packet.
     */
    val id = meta.id

    /**
     * The flow direction of this packet (e.g., client-to-server or server-to-client).
     */
    val flow = meta.flow

    /**
     * Supported protocols for this packet.
     */
    val protocols = meta.protocols

    /**
     * A session identifier for the packet, generated randomly for each instance.
     */
    val sessionId = ThreadLocalRandom.current().nextLong()

    /**
     * Indicates whether the packet is skippable.
     * If true, the packet will be ignored if its size exceeds the allowed limit.
     */
    open val skippable = false

    /**
     * Additional packets that should be sent after this packet, if any.
     */
    open val extraPackets: List<NettyPacket>? = null

    /**
     * Custom logic to handle situations where the packet size exceeds limits.
     *
     * @param connection The connection associated with the packet.
     * @return `true` if the situation was handled, `false` otherwise.
     */
    open fun packetTooLarge(connection: Any): Boolean = false

    /**
     * Determines if the packet is ready for processing.
     *
     * @return `true` if the packet is ready; `false` otherwise.
     */
    open fun isReady(): Boolean = true

    companion object {

        /**
         * Creates a codec for encoding and decoding a specific type of packet.
         *
         * @param encoder The encoder for the packet.
         * @param decoder The decoder for the packet.
         * @return A [StreamCodec] for the specified packet type.
         */
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

    @OptIn(InternalApi::class)
    override fun toString(): String = runCatching {
        ToStringBuilder.reflectionToString(this).toString()
    }.getOrElse { "NettyPacket(id=$id, flow=$flow, skippable=$skippable, extraPackets=$extraPackets, terminal=$terminal, sessionId=$sessionId, handled=$handled)" }
}