package dev.slne.surf.cloud.core.netty.protocol.packets.cloud.info

import dev.slne.surf.cloud.api.meta.DefaultIds
import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.server.CloudServer
import dev.slne.surf.cloud.core.server.CloudServerImpl


@SurfNettyPacket(id = DefaultIds.CLOUD_SERVER_INFO_PACKET)
class CloudServerInfoPacket : NettyPacket<CloudServerInfoPacket> {
    lateinit var action: CloudServerInfoAction
        private set
    lateinit var server: CloudServer
        private set

    internal constructor()

    constructor(action: CloudServerInfoAction, server: CloudServer) {
        this.action = action
        this.server = server
    }

    override fun encode(buffer: SurfByteBuf) {
        buffer.writeWithCodec(CloudServerImpl.CODEC, server)
        buffer.writeEnum(action)
    }

    override fun decode(buffer: SurfByteBuf): CloudServerInfoPacket {
        server = buffer.readWithCodec(CloudServerImpl.CODEC)
        action = buffer.readEnum(CloudServerInfoAction::class)
        return this
    }
}
