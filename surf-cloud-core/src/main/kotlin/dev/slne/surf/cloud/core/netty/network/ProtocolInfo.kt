package dev.slne.surf.cloud.core.netty.network

import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.netty.network.protocol.ProtocolInfoBuilder.CodecEntry
import io.netty.buffer.ByteBuf
import java.util.function.Function

interface ProtocolInfo<T: PacketListener> {
    val id: ConnectionProtocol
    val flow: PacketFlow
    val codec: StreamCodec<ByteBuf, NettyPacket>

    interface Unbound<T : PacketListener, B : ByteBuf> {
        val id: ConnectionProtocol
        val flow: PacketFlow

        fun bind(registryBinder: Function<ByteBuf, B>): ProtocolInfo<T>

        interface Mutable<T: PacketListener, B : ByteBuf>: Unbound<T, B> {

            fun <P : NettyPacket> addPacket(
                id: Class<P>,
                codec: StreamCodec<in B, P>
            ): Mutable<T, B>
        }
    }
}

inline fun <T : PacketListener, B : ByteBuf, reified P : NettyPacket> ProtocolInfo.Unbound.Mutable<T, B>.addPacket(codec: StreamCodec<in B, P>) =
    addPacket(P::class.java, codec)