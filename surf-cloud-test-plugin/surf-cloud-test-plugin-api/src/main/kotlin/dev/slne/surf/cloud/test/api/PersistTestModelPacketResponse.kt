package dev.slne.surf.cloud.test.api

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

enum class PersistTestModelResult {
    CREATED,
    UPDATED,
    FAILURE
}

@SurfNettyPacket(id = 0x4E, flow = PacketFlow.CLIENTBOUND, protocols = [ConnectionProtocol.RUNNING])
class SaveTestModelPacketResponse(val testModel: TestModel, val result: PersistTestModelResult) :
    ResponseNettyPacket() {

    companion object {
        val PACKET_CODEC =
            packetCodec(SaveTestModelPacketResponse::write, ::SaveTestModelPacketResponse)
    }

    private constructor(buf: SurfByteBuf) : this(
        TestModel.STREAM_CODEC.decode(buf),
        buf.readEnum(PersistTestModelResult::class)
    )

    fun write(buf: SurfByteBuf) {
        TestModel.STREAM_CODEC.encode(buf, testModel)
        buf.writeEnum(result)
    }

}