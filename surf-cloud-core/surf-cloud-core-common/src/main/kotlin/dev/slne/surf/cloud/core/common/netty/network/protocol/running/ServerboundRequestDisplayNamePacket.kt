package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import java.util.*

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_REQUEST_DISPLAY_NAME_PACKET,
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.DEFAULT
)
class ServerboundRequestDisplayNamePacket(val uuid: UUID) :
    RespondingNettyPacket<ResponseDisplayNamePacketRequestPacket>(),
    InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ServerboundRequestDisplayNamePacket::uuid,
            ::ServerboundRequestDisplayNamePacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleRequestDisplayName(this)
    }
}