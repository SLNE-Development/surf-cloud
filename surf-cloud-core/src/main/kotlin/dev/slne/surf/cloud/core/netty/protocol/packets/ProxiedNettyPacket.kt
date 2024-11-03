package dev.slne.surf.cloud.core.netty.protocol.packets

import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.netty.network.Connection

@SurfNettyPacket(id = DefaultIds.PROXIED_NETTY_PACKET, PacketFlow.SERVERBOUND)
class ProxiedNettyPacket : NettyPacket {
    lateinit var packet: NettyPacket
        private set
    lateinit var source: Connection
        private set
    lateinit var target: Connection
        private set


    constructor(packet: NettyPacket, target: Connection, client: Connection) {
        this.packet = packet
        this.target = target
        this.source = client
    }

//    override fun encode(buffer: SurfByteBuf) {
////        buffer.writeLong(target.)
////        buffer.writeLong(source.serverGuid)
////        packet.encode(buffer)
//    }
//
//    override fun decode(buffer: SurfByteBuf): ProxiedNettyPacket {
//        val targetGuid = buffer.readLong()
//        val sourceGuid = buffer.readLong()
//        val packetId = buffer.readInt()
//
//        val sourceTracker = client.connection.clientTracker
//        this.target = sourceTracker.findByServerGuid(targetGuid) ?: error("Target not found")
//        this.source = sourceTracker.findByServerGuid(sourceGuid) ?: error("Source not found")
//
//        val createdPacket = client.createPacket(packetId)
//        this.packet = createdPacket ?: error("Packet not found. PacketId: $packetId")
//
//        packet.decode(buffer)
//        return this
//    }
}
