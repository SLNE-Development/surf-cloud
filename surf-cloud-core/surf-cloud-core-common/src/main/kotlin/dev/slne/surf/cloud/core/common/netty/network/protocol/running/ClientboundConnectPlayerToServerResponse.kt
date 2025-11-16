package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum

@SurfNettyPacket(DefaultIds.CLIENTBOUND_CONNECT_PLAYER_TO_SERVER_RESPONSE, PacketFlow.CLIENTBOUND)
class ClientboundConnectPlayerToServerResponse(val result: ConnectionResultEnum) :
    ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = ConnectionResultEnum.STREAM_CODEC.map(
            ::ClientboundConnectPlayerToServerResponse,
            ClientboundConnectPlayerToServerResponse::result
        )
    }
}