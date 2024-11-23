package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

@SurfNettyPacket(DefaultIds.SERVERBOUND_REQUEST_SERVERS_BY_CATEGORY, PacketFlow.SERVERBOUND)
class ServerboundRequestServersByCategory :
    RespondingNettyPacket<ClientboundResponseRequestServersByCategory> {

    companion object {
        val STREAM_CODEC = packetCodec(
            ServerboundRequestServersByCategory::write,
            ::ServerboundRequestServersByCategory
        )
    }

    val category: String

    constructor(category: String) {
        this.category = category
    }

    private constructor(buffer: SurfByteBuf) {
        this.category = buffer.readUtf()
    }

    private fun write(buffer: SurfByteBuf) {
        buffer.writeUtf(category)
    }
}