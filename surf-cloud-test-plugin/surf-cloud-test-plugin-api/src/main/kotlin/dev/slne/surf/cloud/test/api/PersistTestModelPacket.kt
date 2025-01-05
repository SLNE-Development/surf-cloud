package dev.slne.surf.cloud.test.api

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf


@SurfNettyPacket(id = 0x4E, flow = PacketFlow.SERVERBOUND, protocols = [ConnectionProtocol.RUNNING])
class PersistTestModelPacket(val testModel: TestModel) :
    RespondingNettyPacket<SaveTestModelPacketResponse>() {

    companion object {
        val PACKET_CODEC = packetCodec(PersistTestModelPacket::write, ::PersistTestModelPacket)
    }

    private constructor(buf: SurfByteBuf) : this(TestModel.STREAM_CODEC.decode(buf))

    fun write(buf: SurfByteBuf) {
        TestModel.STREAM_CODEC.encode(buf, testModel)
    }

}