@file:OptIn(ExperimentalTypeInference::class, ExperimentalContracts::class)

package dev.slne.surf.cloud.core.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.netty.network.codec.IdDispatchCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.getPacketMeta
import io.netty.buffer.ByteBuf
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

class ProtocolCodecBuilder<B : ByteBuf>(side: PacketFlow) {
    private val dispatchBuilder =
        IdDispatchCodec.builder<B, NettyPacket, Class<out NettyPacket>> { it.javaClass }
    private val flow = side

    fun <T : NettyPacket> add(
        id: Class<out T>,
        codec: StreamCodec<in B, out T>
    ) = apply {
        val meta = id.getPacketMeta()
        check(meta.flow == this.flow || meta.flow == PacketFlow.BIDIRECTIONAL) { "Invalid packet flow for packet $id, expected ${flow.name}" }
        dispatchBuilder.add(id, codec)
    }

    fun build(): StreamCodec<B, NettyPacket> = dispatchBuilder.build { it.getPacketMeta().id }
}

fun <B : ByteBuf> buildProtocolCodec(
    side: PacketFlow,
    @BuilderInference block: ProtocolCodecBuilder<B>.() -> Unit
): StreamCodec<B, NettyPacket> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return ProtocolCodecBuilder<B>(side).apply(block).build()
}