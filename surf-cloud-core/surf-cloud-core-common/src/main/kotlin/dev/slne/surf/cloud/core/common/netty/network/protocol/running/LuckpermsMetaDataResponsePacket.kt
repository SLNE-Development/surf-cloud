package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.writeNullableString

@SurfNettyPacket(DefaultIds.LUCKPERMS_META_DATA_RESPONSE_PACKET, PacketFlow.BIDIRECTIONAL)
class LuckpermsMetaDataResponsePacket : ResponseNettyPacket {

    companion object {
        val STREAM_CODEC =
            packetCodec(LuckpermsMetaDataResponsePacket::write, ::LuckpermsMetaDataResponsePacket)
    }

    val data: String?

    constructor(data: String?) {
        this.data = data
    }

    private constructor(buf: SurfByteBuf) {
        this.data = buf.readNullableString()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeNullableString(data)
    }
}