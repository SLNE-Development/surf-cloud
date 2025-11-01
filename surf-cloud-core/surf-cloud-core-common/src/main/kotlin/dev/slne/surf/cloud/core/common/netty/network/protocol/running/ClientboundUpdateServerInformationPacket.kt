package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

@SurfNettyPacket(DefaultIds.CLIENTBOUND_UPDATE_SERVER_INFORMATION_PACKET, PacketFlow.CLIENTBOUND)
class ClientboundUpdateServerInformationPacket : NettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(
            ClientboundUpdateServerInformationPacket::write,
            ::ClientboundUpdateServerInformationPacket
        )
    }

    val serverName: String
    val information: ClientInformation

    constructor(serverName: String, information: ClientInformation) {
        this.serverName = serverName
        this.information = information
    }

    private constructor(buf: SurfByteBuf) {
        serverName = buf.readUtf()
        information = ClientInformation.STREAM_CODEC.decode(buf)
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUtf(serverName)
        ClientInformation.STREAM_CODEC.encode(buf, information)
    }
}