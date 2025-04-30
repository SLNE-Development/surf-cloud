package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.core.common.player.PunishmentCacheImpl
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@SurfNettyPacket(
    "cloud:serverbound:get_current_login_validation_punishment_cache",
    PacketFlow.SERVERBOUND
)
class ServerboundGetCurrentLoginValidationPunishmentCachePacket(val uuid: @Contextual UUID) :
    RespondingNettyPacket<ClientboundGetCurrentLoginValidationPunishmentCacheResponsePacket>()

@Serializable
@SurfNettyPacket(
    "cloud:clientbound:get_current_login_validation_punishment_cache_response",
    PacketFlow.CLIENTBOUND
)
class ClientboundGetCurrentLoginValidationPunishmentCacheResponsePacket(val cache: PunishmentCacheImpl?) :
    ResponseNettyPacket()