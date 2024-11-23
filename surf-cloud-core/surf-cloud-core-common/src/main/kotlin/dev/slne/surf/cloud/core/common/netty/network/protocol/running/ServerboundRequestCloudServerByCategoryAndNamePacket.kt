package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

@SurfNettyPacket(DefaultIds.SERVERBOUND_REQUEST_CLOUD_SERVER_BY_CATEGORY_AND_NAME, PacketFlow.SERVERBOUND)
class ServerboundRequestCloudServerByCategoryAndNamePacket: RespondingNettyPacket<ClientboundResponseCloudServerPacket> {

    companion object {
        val STREAM_CODEC = packetCodec(
            ServerboundRequestCloudServerByCategoryAndNamePacket::write,
            ::ServerboundRequestCloudServerByCategoryAndNamePacket
        )
    }

    val category: String
    val name: String

    constructor(category: String, name: String) {
        this.category = category
        this.name = name
    }

    private constructor(buf: SurfByteBuf) {
        category = buf.readUtf()
        name = buf.readUtf()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUtf(category)
        buf.writeUtf(name)
    }
}