package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.common.CommonRunningPacketListener
import java.util.*

@SurfNettyPacket(
    DefaultIds.REQUEST_LUCKPERMS_META_DATA_PACKET,
    PacketFlow.BIDIRECTIONAL,
    handlerMode = PacketHandlerMode.DEFAULT
)
class RequestLuckpermsMetaDataPacket(
    val uuid: UUID,
    val key: String
) : RespondingNettyPacket<LuckpermsMetaDataResponsePacket>(),
    InternalNettyPacket<CommonRunningPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            RequestLuckpermsMetaDataPacket::uuid,
            ByteBufCodecs.STRING_CODEC,
            RequestLuckpermsMetaDataPacket::key,
            ::RequestLuckpermsMetaDataPacket
        )
    }

    override fun handle(listener: CommonRunningPacketListener) {
        listener.handleRequestLuckpermsMetaData(this)
    }
}