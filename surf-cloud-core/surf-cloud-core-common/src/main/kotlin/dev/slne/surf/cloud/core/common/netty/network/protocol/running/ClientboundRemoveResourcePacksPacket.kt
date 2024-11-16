package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import java.util.UUID

@SurfNettyPacket(DefaultIds.CLIENTBOUND_REMOVE_RESOURCE_PACKS_PACKET, PacketFlow.CLIENTBOUND)
class ClientboundRemoveResourcePacksPacket: NettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(ClientboundRemoveResourcePacksPacket::write, ::ClientboundRemoveResourcePacksPacket)
    }

    val uuid: UUID
    val first: UUID
    val others: Array<out UUID>

    constructor(uuid: UUID, first: UUID, vararg others: UUID) {
        this.uuid = uuid
        this.first = first
        this.others = others
    }

    private constructor(buf: SurfByteBuf) {
        this.uuid = buf.readUuid()
        this.first = buf.readUuid()
        this.others = buf.readUuidArray()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(uuid)
        buf.writeUuid(first)
        buf.writeUuidArray(others)
    }
}