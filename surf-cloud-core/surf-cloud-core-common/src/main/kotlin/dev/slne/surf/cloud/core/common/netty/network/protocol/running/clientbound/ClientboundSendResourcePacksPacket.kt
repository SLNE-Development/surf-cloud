package dev.slne.surf.cloud.core.common.netty.network.protocol.running.clientbound

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.util.codec.ExtraCodecs
import net.kyori.adventure.resource.ResourcePackRequest
import java.util.UUID

@SurfNettyPacket(DefaultIds.CLIENTBOUND_SEND_RESOURCE_PACKS_PACKET, PacketFlow.CLIENTBOUND)
class ClientboundSendResourcePacksPacket : NettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(
            ClientboundSendResourcePacksPacket::write,
            ::ClientboundSendResourcePacksPacket
        )
    }

    val uuid: UUID
    val request: ResourcePackRequest

    constructor(uuid: UUID, request: ResourcePackRequest) {
        this.uuid = uuid
        this.request = request
    }

    private constructor(buf: SurfByteBuf) {
        this.uuid = buf.readUuid()
        this.request = ExtraCodecs.STREAM_RESOURCE_PACK_REQUEST_CODEC.decode(buf)
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(uuid)
        ExtraCodecs.STREAM_RESOURCE_PACK_REQUEST_CODEC.encode(buf, request)
    }
}