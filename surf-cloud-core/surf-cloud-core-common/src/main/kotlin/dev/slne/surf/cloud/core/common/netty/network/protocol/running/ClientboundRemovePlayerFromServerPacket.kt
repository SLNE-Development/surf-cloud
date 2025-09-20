package dev.slne.surf.cloud.core.common.netty.network.protocol.running

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

    val serverName: String
    val playerUuid: UUID

    constructor(serverName: String, playerUuid: UUID) {
        this.serverName = serverName
        this.playerUuid = playerUuid
    }

    private constructor(buf: SurfByteBuf) {
        serverName = buf.readUtf()
        playerUuid = buf.readUuid()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUtf(serverName)
        buf.writeUuid(playerUuid)
    }
}