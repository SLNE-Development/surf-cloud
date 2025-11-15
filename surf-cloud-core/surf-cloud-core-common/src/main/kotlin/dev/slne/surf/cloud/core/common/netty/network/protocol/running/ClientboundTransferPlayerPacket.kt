package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import java.net.InetSocketAddress
import java.util.*

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_TRANSFER_PLAYER_PACKET,
    PacketFlow.CLIENTBOUND,
    handlerMode = PacketHandlerMode.DEFAULT
)
class ClientboundTransferPlayerPacket(val playerUuid: UUID, val address: InetSocketAddress) :
    RespondingNettyPacket<ServerboundTransferPlayerPacketResponse>(),
    InternalNettyPacket<RunningClientPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ClientboundTransferPlayerPacket::playerUuid,
            ByteBufCodecs.INET_SOCKET_ADDRESS_CODEC,
            ClientboundTransferPlayerPacket::address,
            ::ClientboundTransferPlayerPacket
        )
    }

    override fun handle(listener: RunningClientPacketListener) {
        listener.handleTransferPlayer(this)
    }
}