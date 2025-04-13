package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.boolean.BooleanResponsePacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@SurfNettyPacket("cloud:bidirectional:teleport_player_to_player", PacketFlow.BIDIRECTIONAL)
@Serializable
class TeleportPlayerToPlayerPacket(
    val uuid: @Contextual UUID,
    val target: @Contextual UUID,
) : BooleanResponsePacket()