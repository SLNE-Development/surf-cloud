package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

@SurfNettyPacket(DefaultIds.SERVERBOUND_REQUEST_CLOUD_SERVER_BY_NAME, PacketFlow.SERVERBOUND)
class ServerboundRequestCloudServerByNamePacket: RespondingNettyPacket<ClientboundResponseCloudServerPacket> {

    companion object {
        val STREAM_CODEC = packetCodec(ServerboundRequestCloudServerByNamePacket::write, ::ServerboundRequestCloudServerByNamePacket)
    }

    val name: String

    constructor(name: String) {
        this.name = name
    }

    private constructor(buffer: SurfByteBuf) {
        this.name = buffer.readUtf()
    }

    private fun write(buffer: SurfByteBuf) {
        buffer.writeUtf(name)
    }
}