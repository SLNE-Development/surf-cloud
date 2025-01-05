package dev.slne.surf.cloud.test.api

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

@SurfNettyPacket(id = 0x50, flow = PacketFlow.CLIENTBOUND, protocols = [ConnectionProtocol.RUNNING])
class FindTestModelPacketResponse(val testModel: TestModel) : ResponseNettyPacket() {

    companion object {
        val PACKET_CODEC =
            packetCodec(FindTestModelPacketResponse::write, ::FindTestModelPacketResponse)
    }

    private constructor(buf: SurfByteBuf) : this(TestModel.STREAM_CODEC.decode(buf))

    fun write(buf: SurfByteBuf) {
        TestModel.STREAM_CODEC.encode(buf, testModel)
    }

}