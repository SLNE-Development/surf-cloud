package dev.slne.surf.cloud.core.common.netty.network.protocol.running.clientbound

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import java.util.*

@SurfNettyPacket(DefaultIds.CLIENTBOUND_REMOVE_PLAYER_FROM_SERVER, PacketFlow.CLIENTBOUND)
class ClientboundRemovePlayerFromServerPacket : NettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(
            ClientboundRemovePlayerFromServerPacket::write,
            ::ClientboundRemovePlayerFromServerPacket
        )
    }

    val serverUid: Long
    val playerUuid: UUID

    constructor(serverUid: Long, playerUuid: UUID) {
        this.serverUid = serverUid
        this.playerUuid = playerUuid
    }

    private constructor(buf: SurfByteBuf) {
        serverUid = buf.readLong()
        playerUuid = buf.readUuid()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeLong(serverUid)
        buf.writeUuid(playerUuid)
    }
}