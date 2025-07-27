package dev.slne.surf.cloud.core.common.netty.network.protocol.running.clientbound

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.serverbound.ClientInformation

@SurfNettyPacket(DefaultIds.CLIENTBOUND_UPDATE_SERVER_INFORMATION_PACKET, PacketFlow.CLIENTBOUND)
class ClientboundUpdateServerInformationPacket : NettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(
            ClientboundUpdateServerInformationPacket::write,
            ::ClientboundUpdateServerInformationPacket
        )
    }

    val serverId: Long
    val information: ClientInformation

    constructor(serverId: Long, information: ClientInformation) {
        this.serverId = serverId
        this.information = information
    }

    private constructor(buf: SurfByteBuf) {
        serverId = buf.readVarLong()
        information = ClientInformation.Companion.STREAM_CODEC.decode(buf)
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeVarLong(serverId)
        ClientInformation.Companion.STREAM_CODEC.encode(buf, information)
    }
}