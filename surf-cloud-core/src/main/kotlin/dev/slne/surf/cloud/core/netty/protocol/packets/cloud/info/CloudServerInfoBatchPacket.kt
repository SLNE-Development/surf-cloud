package dev.slne.surf.cloud.core.netty.protocol.packets.cloud.info

import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.server.CloudServer
import dev.slne.surf.cloud.core.server.CloudServerImpl

@SurfNettyPacket(id = DefaultIds.CLOUD_SERVER_INFO_BATCH_PACKET)
class CloudServerInfoBatchPacket : NettyPacket<CloudServerInfoBatchPacket> {
    lateinit var servers: List<CloudServer>
        private set
    lateinit var action: CloudServerInfoAction
        private set

    internal constructor()

    constructor(action: CloudServerInfoAction, servers: Iterable<CloudServer>) {
        this.action = action
        this.servers = servers.toList()
    }

    constructor(action: CloudServerInfoAction, vararg servers: CloudServer) {
        this.action = action
        this.servers = servers.toList()
    }

    override fun encode(buffer: SurfByteBuf) {
        buffer.writeEnum(action)
        buffer.writeCollectionWithCodec(CloudServerImpl.CODEC, servers)
    }

    override fun decode(buffer: SurfByteBuf): CloudServerInfoBatchPacket? {
        action = buffer.readEnum(CloudServerInfoAction::class.java)
        servers = buffer.readListWithCodec(CloudServerImpl.CODEC)
        return this
    }
}
