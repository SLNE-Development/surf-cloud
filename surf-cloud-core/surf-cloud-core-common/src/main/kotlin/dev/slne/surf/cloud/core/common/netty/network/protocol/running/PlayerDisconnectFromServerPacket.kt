package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import java.util.UUID

@SurfNettyPacket(DefaultIds.PLAYER_DISCONNECT_FROM_SERVER_PACKET, PacketFlow.BIDIRECTIONAL)
class PlayerDisconnectFromServerPacket: NettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(PlayerDisconnectFromServerPacket::write, ::PlayerDisconnectFromServerPacket)
    }

    val uuid: UUID
    val serverUid: Long
    val proxy: Boolean

    constructor(uuid: UUID, serverUid: Long, proxy: Boolean) {
        this.uuid = uuid
        this.serverUid = serverUid
        this.proxy = proxy
    }

    private constructor(buffer: SurfByteBuf) {
        this.uuid = buffer.readUuid()
        this.serverUid = buffer.readLong()
        this.proxy = buffer.readBoolean()
    }

    private fun write(buffer: SurfByteBuf) {
        buffer.writeUuid(uuid)
        buffer.writeLong(serverUid)
        buffer.writeBoolean(proxy)
    }
}