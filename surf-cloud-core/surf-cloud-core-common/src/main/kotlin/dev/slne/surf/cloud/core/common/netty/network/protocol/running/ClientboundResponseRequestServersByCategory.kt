package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.common.server.CloudServerImpl
import it.unimi.dsi.fastutil.objects.ObjectList

@SurfNettyPacket(DefaultIds.CLIENTBOUND_RESPONSE_REQUEST_SERVERS_BY_CATEGORY, PacketFlow.CLIENTBOUND)
class ClientboundResponseRequestServersByCategory : ResponseNettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(ClientboundResponseRequestServersByCategory::write, ::ClientboundResponseRequestServersByCategory)
    }

    val servers: ObjectList<out CloudServerImpl>

    constructor(servers: ObjectList<out CloudServerImpl>) {
        this.servers = servers
    }

    private constructor(buffer: SurfByteBuf) {
        this.servers = buffer.readList { CloudServerImpl.STREAM_CODEC.decode(it) }
    }

    private fun write(buffer: SurfByteBuf) {
        buffer.writeCollection(servers, CloudServerImpl.STREAM_CODEC::encode)
    }
}