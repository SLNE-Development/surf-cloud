@file:OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)

package dev.slne.surf.cloud.core.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecUnitSimple
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.network.ClientboundPacketListener
import dev.slne.surf.cloud.core.common.netty.network.PacketListener
import dev.slne.surf.cloud.core.common.netty.network.ProtocolInfo
import dev.slne.surf.cloud.core.common.netty.network.ServerboundPacketListener
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
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
    private val codecs = mutableObjectSetOf<CodecEntry<*, B>>()
    private var bundlerInfo: BundlerInfo? = null

    fun <P : NettyPacket> addPacket(
        id: Class<P>,
        codec: StreamCodec<in B, out P>
    ) = apply { codecs.add(CodecEntry(id, codec)) }

    inline fun <reified P : NettyPacket> addPacket(codec: StreamCodec<in B, P>) =
        addPacket(P::class.java, codec)

    fun <P : BundlePacket, D : BundleDelimiterPacket> withBundlePacket(
        id: Class<P>,
        bundler: (Iterable<NettyPacket>) -> P,
        splitter: D
    ) = apply {
        val streamCodec = streamCodecUnitSimple(splitter)
        codecs.add(CodecEntry(splitter::class.java, streamCodec))
        this.bundlerInfo = BundlerInfo.createForPacket(id, bundler, splitter)
    }

    inline fun <reified P : BundlePacket, reified D : BundleDelimiterPacket> withBundlePacket(
        noinline bundler: (Iterable<NettyPacket>) -> P,
        splitter: D
    ) = withBundlePacket(P::class.java, bundler, splitter)

    fun buildPacketCodec(
        bufUpgrader: Function<ByteBuf, B>,
        packetTypes: Iterable<CodecEntry<*, B>>
    ) = buildProtocolCodec(this.flow) {
        for (codecEntry in packetTypes) {
            codecEntry.addToBuilder(this, bufUpgrader)
        }
    }

    fun build(bufUpgrader: Function<ByteBuf, B>): ProtocolInfo<T> =
        ProtocolInfoImpl(
            this.protocol,
            this.flow,
            this.buildPacketCodec(bufUpgrader, this.codecs),
            this.bundlerInfo
        )

    fun buildUnbound() = object : ProtocolInfo.Unbound<T, B> {
        override val id = protocol
        override val flow = this@ProtocolInfoBuilder.flow
        override fun bind(registryBinder: Function<ByteBuf, B>): ProtocolInfo<T> =
            ProtocolInfoImpl(protocol, flow, buildPacketCodec(registryBinder, codecs), bundlerInfo)
    }

    fun buildUnboundMutable() = object : ProtocolInfo.Unbound.Mutable<T, B> {
        private var frozen = false

        override fun freeze(): ProtocolInfo.Unbound<T, B> {
            frozen = true
            return buildUnbound()
        }

        override val id = protocol
        override val flow = this@ProtocolInfoBuilder.flow
        override fun bind(registryBinder: Function<ByteBuf, B>): ProtocolInfo<T> =
            ProtocolInfoImpl(protocol, flow, buildPacketCodec(registryBinder, codecs), bundlerInfo)

        override fun <P : NettyPacket> addPacket(
            id: Class<P>,
            codec: StreamCodec<in B, out P>
        ) = apply {
            check(!frozen) { "Cannot add packets after freezing" }
            this@ProtocolInfoBuilder.addPacket(id, codec)
        }
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

    internal data class ProtocolInfoImpl<L : PacketListener>(
        override val id: ConnectionProtocol,
        override val flow: PacketFlow,
        override val codec: StreamCodec<ByteBuf, NettyPacket>,
        override val bundlerInfo: BundlerInfo?
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

fun <T : ClientboundPacketListener, B : ByteBuf> buildClientProtocolInfo(
    protocol: ConnectionProtocol,
    @BuilderInference block: ProtocolInfoBuilder<T, B>.() -> Unit
) = ProtocolInfoBuilder.clientboundProtocol<T, B>(protocol, block)

fun <T : ServerboundPacketListener, B : ByteBuf> buildServerProtocolInfo(
    protocol: ConnectionProtocol,
    @BuilderInference block: ProtocolInfoBuilder<T, B>.() -> Unit
) = ProtocolInfoBuilder.serverboundProtocol<T, B>(protocol, block)