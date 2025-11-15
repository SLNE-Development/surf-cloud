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
    DefaultIds.CLIENTBOUND_REQUEST_DISPLAY_NAME_PACKET,
    PacketFlow.CLIENTBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ClientboundRequestDisplayNamePacket(val uuid: UUID) :
    RespondingNettyPacket<ResponseDisplayNamePacketRequestPacket>(),
    InternalNettyPacket<RunningClientPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ClientboundRequestDisplayNamePacket::uuid,
            ::ClientboundRequestDisplayNamePacket
        )
    }

    override fun handle(listener: RunningClientPacketListener) {
        listener.handleRequestDisplayName(this)
    }
}