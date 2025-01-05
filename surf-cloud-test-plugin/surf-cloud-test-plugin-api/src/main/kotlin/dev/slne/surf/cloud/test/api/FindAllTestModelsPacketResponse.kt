package dev.slne.surf.cloud.test.api

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.util.mutableObjectSetOf
import dev.slne.surf.cloud.api.common.util.toObjectSet
import it.unimi.dsi.fastutil.objects.ObjectSet

@SurfNettyPacket(id = 0x52, flow = PacketFlow.CLIENTBOUND, protocols = [ConnectionProtocol.RUNNING])
class FindAllTestModelsPacketResponse(
    val testModels: ObjectSet<TestModel>
) : ResponseNettyPacket() {

    companion object {
        val PACKET_CODEC =
            packetCodec(FindAllTestModelsPacketResponse::write, ::FindAllTestModelsPacketResponse)
    }

    private constructor(buf: SurfByteBuf) : this(mutableObjectSetOf()) {
        testModels.addAll(
            buf.readArray { TestModel.STREAM_CODEC.decode(it) }.toObjectSet()
        )
    }

    fun write(buf: SurfByteBuf) {
        buf.writeArray(testModels.toTypedArray()) { arrayBuf, model ->
            TestModel.STREAM_CODEC.encode(
                arrayBuf,
                model
            )
        }
    }

}