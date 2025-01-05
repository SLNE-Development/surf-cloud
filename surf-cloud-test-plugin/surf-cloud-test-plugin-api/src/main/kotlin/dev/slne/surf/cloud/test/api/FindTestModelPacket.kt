package dev.slne.surf.cloud.test.api

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf


@SurfNettyPacket(id = 0x4F, flow = PacketFlow.SERVERBOUND, protocols = [ConnectionProtocol.RUNNING])
class FindTestModelPacket(val name: String) : RespondingNettyPacket<FindTestModelPacketResponse>() {

    companion object {
        val PACKET_CODEC = packetCodec(FindTestModelPacket::write, ::FindTestModelPacket)
    }

    private constructor(buf: SurfByteBuf) : this(buf.readUtf())

    fun write(buf: SurfByteBuf) {
        buf.writeUtf(name)
    }

}