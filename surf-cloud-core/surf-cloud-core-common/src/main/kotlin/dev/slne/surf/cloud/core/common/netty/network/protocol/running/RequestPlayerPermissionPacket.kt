package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.boolean.BooleanResponsePacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@SurfNettyPacket("cloud:bidirectional:request_player_permission", PacketFlow.BIDIRECTIONAL)
class RequestPlayerPermissionPacket(val uuid: @Contextual UUID, val permission: String) :
    BooleanResponsePacket()