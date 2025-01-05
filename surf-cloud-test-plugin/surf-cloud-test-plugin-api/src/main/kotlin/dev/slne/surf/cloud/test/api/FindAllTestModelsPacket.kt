package dev.slne.surf.cloud.test.api

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf


@SurfNettyPacket(id = 0x51, flow = PacketFlow.SERVERBOUND, protocols = [ConnectionProtocol.RUNNING])
class FindAllTestModelsPacket() : RespondingNettyPacket<FindAllTestModelsPacketResponse>() {

    companion object {
        val PACKET_CODEC = packetCodec(FindAllTestModelsPacket::write, ::FindAllTestModelsPacket)
    }

    private constructor(buf: SurfByteBuf) : this()

    fun write(buf: SurfByteBuf) {}

}