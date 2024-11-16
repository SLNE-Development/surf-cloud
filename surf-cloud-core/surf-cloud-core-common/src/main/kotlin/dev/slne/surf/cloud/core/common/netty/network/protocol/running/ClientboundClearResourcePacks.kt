package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import java.util.UUID

@SurfNettyPacket(DefaultIds.CLIENTBOUND_CLEAR_RESOURCE_PACKS_PACKET, PacketFlow.CLIENTBOUND)
class ClientboundClearResourcePacks: NettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(ClientboundClearResourcePacks::write, ::ClientboundClearResourcePacks)
    }

    val uuid: UUID

    constructor(uuid: UUID) {
        this.uuid = uuid
    }

    private constructor(buf: SurfByteBuf) {
        this.uuid = buf.readUuid()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(uuid)
    }
}