package dev.slne.surf.cloud.core.netty.protocol.packets

import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.netty.source.NettyServerSource
import dev.slne.surf.cloud.core.coreCloudInstance
import dev.slne.surf.cloud.core.netty.client.SurfNettyClient

@SurfNettyPacket(id = DefaultIds.PROXIED_NETTY_PACKET)
class ProxiedNettyPacket : NettyPacket<ProxiedNettyPacket> {
    private val client: SurfNettyClient
    lateinit var packet: NettyPacket<*>
        private set
    lateinit var source: NettyServerSource
        private set
    lateinit var target: NettyServerSource
        private set

    internal constructor() {
        this.client = coreCloudInstance.dataContext.getBean(SurfNettyClient::class.java)
    }

    constructor(packet: NettyPacket<*>, target: NettyServerSource, client: SurfNettyClient) {
        this.packet = packet
        this.target = target
        this.source = client.connection.source
        this.client = client
    }

    override fun encode(buffer: SurfByteBuf) {
        buffer.writeLong(target.serverGuid)
        buffer.writeLong(source.serverGuid)
        packet.encode(buffer)
    }

    override fun decode(buffer: SurfByteBuf): ProxiedNettyPacket {
        val targetGuid = buffer.readLong()
        val sourceGuid = buffer.readLong()
        val packetId = buffer.readInt()

        val sourceTracker = client.connection.clientTracker
        this.target = sourceTracker.findByServerGuid(targetGuid) ?: error("Target not found")
        this.source = sourceTracker.findByServerGuid(sourceGuid) ?: error("Source not found")

        val createdPacket = client.createPacket(packetId)
        this.packet = createdPacket ?: error("Packet not found. PacketId: $packetId")

        packet.decode(buffer)
        return this
    }
}
