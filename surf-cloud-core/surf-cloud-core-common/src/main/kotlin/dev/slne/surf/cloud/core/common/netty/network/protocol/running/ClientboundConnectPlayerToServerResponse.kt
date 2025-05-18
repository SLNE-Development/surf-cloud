package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.player.ConnectionResultEnum
import kotlinx.serialization.Serializable

@SurfNettyPacket(DefaultIds.CLIENTBOUND_CONNECT_PLAYER_TO_SERVER_RESPONSE, PacketFlow.CLIENTBOUND)
@Serializable
class ClientboundConnectPlayerToServerResponse(val result: ConnectionResultEnum) :
    ResponseNettyPacket()