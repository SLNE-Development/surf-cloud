package dev.slne.surf.cloud.core.netty.protocol.packets

import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.packet.packetCodec
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.netty.network.protocol.running.RunningProtocols

@SurfNettyPacket(DefaultIds.SERVERBOUND_BROADCAST_PACKET, PacketFlow.SERVERBOUND)
class ServerboundBroadcastPacket : NettyPacket {
    companion object {
        val STREAM_CODEC =
            packetCodec(ServerboundBroadcastPacket::write, ::ServerboundBroadcastPacket)
    }

    val packet: NettyPacket

    constructor(packet: NettyPacket) {
        this.packet = packet
    }

    private constructor(buffer: SurfByteBuf) {
        packet = RunningProtocols.SERVERBOUND_LAZY.codec.decode(buffer)
    }

    private fun write(buffer: SurfByteBuf) {
        RunningProtocols.SERVERBOUND_LAZY.codec.encode(buffer, packet)
    }
}