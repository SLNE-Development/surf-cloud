package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import java.util.*

@SurfNettyPacket(DefaultIds.SERVERBOUND_CONNECT_PLAYER_TO_SERVER, PacketFlow.SERVERBOUND)
class ServerboundConnectPlayerToServerPacket :
    RespondingNettyPacket<ClientboundConnectPlayerToServerResponse> {

    companion object {
        val STREAM_CODEC =
            packetCodec(ServerboundConnectPlayerToServerPacket::write, ::ServerboundConnectPlayerToServerPacket)
    }

    val uuid: UUID
    val serverId: Long
    val queue: Boolean

    constructor(uuid: UUID, serverId: Long, queue: Boolean) {
        this.uuid = uuid
        this.serverId = serverId
        this.queue = queue
    }

    private constructor(buf: SurfByteBuf) {
        uuid = buf.readUuid()
        serverId = buf.readLong()
        queue = buf.readBoolean()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(uuid)
        buf.writeLong(serverId)
        buf.writeBoolean(queue)
    }
}