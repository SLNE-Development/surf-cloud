package dev.slne.surf.cloud.core.netty.protocol.packets

import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.netty.common.registry.packet.NettyPacketRegistry

class ServerboundBroadcastPacket : NettyPacket {
    lateinit var packet: NettyPacket

    internal constructor()

    constructor(packet: NettyPacket) {
        this.packet = packet
    }

    override fun encode(buffer: SurfByteBuf) {
        buffer.writeVarInt(packet.id)
        packet.encode(buffer)
    }

    override fun decode(buffer: SurfByteBuf): ServerboundBroadcastPacket {
        val packetId = buffer.readVarInt()
        val createdPacket = NettyPacketRegistry.createPacket(packetId)
        this.packet = createdPacket ?: error("Packet not found. PacketId: $packetId")
        packet.decode(buffer)

        return this
    }
}