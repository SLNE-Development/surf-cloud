@file:OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)

package dev.slne.surf.cloud.core.netty.network.protocol

import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.util.mutableObjectListOf
import dev.slne.surf.cloud.core.netty.network.ClientboundPacketListener
import dev.slne.surf.cloud.core.netty.network.PacketListener
import dev.slne.surf.cloud.core.netty.network.ProtocolInfo
import dev.slne.surf.cloud.core.netty.network.ServerboundPacketListener
import io.netty.buffer.ByteBuf
import java.util.function.Consumer
import java.util.function.Function
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

class ProtocolInfoBuilder<T : PacketListener, B : ByteBuf>(
    val protocol: ConnectionProtocol,
    val flow: PacketFlow
) {
    private val codecs = mutableObjectListOf<CodecEntry<*, B>>()

    fun <P : NettyPacket> addPacket(
        id: Class<P>,
        codec: StreamCodec<in B, P>
    ) = apply { codecs.add(CodecEntry(id, codec)) }

    inline fun <reified P : NettyPacket> addPacket(codec: StreamCodec<in B, P>) =
        addPacket(P::class.java, codec)

    fun buildPacketCodec(
        bufUpgrader: Function<ByteBuf, B>,
        packetTypes: List<CodecEntry<*, B>>
    ) = buildProtocolCodec(this.flow) {
        for (codecEntry in packetTypes) {
            codecEntry.addToBuilder(this, bufUpgrader)
        }
    }

    fun build(bufUpgrader: Function<ByteBuf, B>): ProtocolInfo<T> =
        ProtocolInfoImpl(this.protocol, this.flow, this.buildPacketCodec(bufUpgrader, this.codecs))

    fun buildUnbound() = object : ProtocolInfo.Unbound<T, B> {
        override val id = protocol
        override val flow = this@ProtocolInfoBuilder.flow
        override fun bind(registryBinder: Function<ByteBuf, B>): ProtocolInfo<T> =
            ProtocolInfoImpl(protocol, flow, buildPacketCodec(registryBinder, codecs))
    }

    fun buildUnboundMutable() = object : ProtocolInfo.Unbound.Mutable<T, B> {
        override val id = protocol
        override val flow = this@ProtocolInfoBuilder.flow
        override fun bind(registryBinder: Function<ByteBuf, B>): ProtocolInfo<T> =
            ProtocolInfoImpl(protocol, flow, buildPacketCodec(registryBinder, codecs))

        override fun <P : NettyPacket> addPacket(
            id: Class<P>,
            codec: StreamCodec<in B, P>
        ) = apply { this@ProtocolInfoBuilder.addPacket(id, codec) }
    }

    companion object {
        private fun <L : PacketListener, B : ByteBuf> protocol(
            type: ConnectionProtocol,
            side: PacketFlow,
            registrar: Consumer<ProtocolInfoBuilder<L, B>>
        ) = ProtocolInfoBuilder<L, B>(type, side).run {
            registrar.accept(this)
            buildUnbound()
        }

        @JvmStatic
        fun <T : ServerboundPacketListener, B : ByteBuf> serverboundProtocol(
            type: ConnectionProtocol, registrar: Consumer<ProtocolInfoBuilder<T, B>>
        ) = protocol(type, PacketFlow.SERVERBOUND, registrar)

        @JvmStatic
        fun <T : ClientboundPacketListener, B : ByteBuf> clientboundProtocol(
            type: ConnectionProtocol, registrar: Consumer<ProtocolInfoBuilder<T, B>>
        ) = protocol(type, PacketFlow.CLIENTBOUND, registrar)

        private fun <L : PacketListener, B : ByteBuf> mutableProtocol(
            type: ConnectionProtocol,
            side: PacketFlow,
            registrar: Consumer<ProtocolInfoBuilder<L, B>>
        ) = ProtocolInfoBuilder<L, B>(type, side).run {
            registrar.accept(this)
            buildUnboundMutable()
        }

        @JvmStatic
        fun <T : ServerboundPacketListener, B : ByteBuf> mutableServerboundProtocol(
            type: ConnectionProtocol, registrar: Consumer<ProtocolInfoBuilder<T, B>>
        ) = mutableProtocol(type, PacketFlow.SERVERBOUND, registrar)

        @JvmStatic
        fun <T : ClientboundPacketListener, B : ByteBuf> mutableClientboundProtocol(
            type: ConnectionProtocol, registrar: Consumer<ProtocolInfoBuilder<T, B>>
        ) = mutableProtocol(type, PacketFlow.CLIENTBOUND, registrar)
    }

    @JvmRecord
    data class CodecEntry<P : NettyPacket, B : ByteBuf>(
        val type: Class<out NettyPacket>,
        val serializer: StreamCodec<in B, P>
    ) {
        fun addToBuilder(
            builder: ProtocolCodecBuilder<ByteBuf>,
            bufUpgrader: Function<ByteBuf, B>
        ) {
            val streamCodec = serializer.mapStream(bufUpgrader)
            builder.add(this.type, streamCodec)
        }
    }

    @JvmRecord
    internal data class ProtocolInfoImpl<L : PacketListener>(
        override val id: ConnectionProtocol,
        override val flow: PacketFlow,
        override val codec: StreamCodec<ByteBuf, NettyPacket>,
    ) : ProtocolInfo<L>
}

fun <T : PacketListener, B : ByteBuf> buildProtocolInfo(
    protocol: ConnectionProtocol,
    flow: PacketFlow,
    @BuilderInference block: ProtocolInfoBuilder<T, B>.() -> Unit
): ProtocolInfoBuilder<T, B> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return ProtocolInfoBuilder<T, B>(protocol, flow).apply(block)
}