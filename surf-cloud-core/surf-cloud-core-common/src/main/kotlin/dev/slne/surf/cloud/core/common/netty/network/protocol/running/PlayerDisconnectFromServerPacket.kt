package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import java.util.*

@SurfNettyPacket(DefaultIds.PLAYER_DISCONNECT_FROM_SERVER_PACKET, PacketFlow.BIDIRECTIONAL)
class PlayerDisconnectFromServerPacket : NettyPacket {

    companion object {
        val STREAM_CODEC =
            packetCodec(PlayerDisconnectFromServerPacket::write, ::PlayerDisconnectFromServerPacket)
    }

    val uuid: UUID
    val serverName: String
    val proxy: Boolean

    constructor(uuid: UUID, serverName: String, proxy: Boolean) {
        this.uuid = uuid
        this.serverName = serverName
        this.proxy = proxy
    }

    private constructor(buffer: SurfByteBuf) {
        this.uuid = buffer.readUuid()
        this.serverName = buffer.readUtf()
        this.proxy = buffer.readBoolean()
    }

    private fun write(buffer: SurfByteBuf) {
        buffer.writeUuid(uuid)
        buffer.writeUtf(serverName)
        buffer.writeBoolean(proxy)
    }
}