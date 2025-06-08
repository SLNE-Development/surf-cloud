package dev.slne.surf.cloud.core.common.netty.network

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.BundlerInfo
import io.netty.buffer.ByteBuf
import java.util.function.Function

interface ProtocolInfo<T: PacketListener> {
    val id: ConnectionProtocol
    val flow: PacketFlow
    val codec: StreamCodec<ByteBuf, NettyPacket>

    val bundlerInfo: BundlerInfo?

    interface Unbound<T : PacketListener, B : ByteBuf> {
        val id: ConnectionProtocol
        val flow: PacketFlow

        fun bind(registryBinder: Function<ByteBuf, B>): ProtocolInfo<T>

        interface Mutable<T: PacketListener, B : ByteBuf>: Unbound<T, B> {
            fun <P : NettyPacket> addPacket(
                id: Class<P>,
                codec: StreamCodec<in B, out P>
            ): Mutable<T, B>

            fun freeze(): Unbound<T, B>
        }
    }
}

inline fun <T : PacketListener, B : ByteBuf, reified P : NettyPacket> ProtocolInfo.Unbound.Mutable<T, B>.addPacket(codec: StreamCodec<in B, P>) =
    addPacket(P::class.java, codec)