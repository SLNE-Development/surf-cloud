package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.common.server.CloudServerImpl

@SurfNettyPacket(DefaultIds.CLIENTBOUND_RESPONSE_CLOUD_SERVER, PacketFlow.CLIENTBOUND)
class ClientboundResponseCloudServerPacket : ResponseNettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(
            ClientboundResponseCloudServerPacket::write,
            ::ClientboundResponseCloudServerPacket
        )
    }

    val server: CloudServerImpl?

    constructor(server: CloudServerImpl?) {
        this.server = server
    }

    private constructor(buf: SurfByteBuf) {
        server = buf.readNullable(CloudServerImpl.STREAM_CODEC::decode)
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeNullable(server, CloudServerImpl.STREAM_CODEC::encode)
    }
}