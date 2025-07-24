package dev.slne.surf.cloud.core.common.netty.network.protocol.running.bidirectional

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import java.util.*

@SurfNettyPacket(DefaultIds.REQUEST_LUCKPERMS_META_DATA_PACKET, PacketFlow.BIDIRECTIONAL)
class RequestLuckpermsMetaDataPacket : RespondingNettyPacket<LuckpermsMetaDataResponsePacket> {

    companion object {
        val STREAM_CODEC =
            packetCodec(RequestLuckpermsMetaDataPacket::write, ::RequestLuckpermsMetaDataPacket)
    }

    val uuid: UUID
    val key: String

    constructor(uuid: UUID, key: String) {
        this.uuid = uuid
        this.key = key
    }

    private constructor(buf: SurfByteBuf) {
        this.uuid = buf.readUuid()
        this.key = buf.readUtf()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(uuid)
        buf.writeUtf(key)
    }
}