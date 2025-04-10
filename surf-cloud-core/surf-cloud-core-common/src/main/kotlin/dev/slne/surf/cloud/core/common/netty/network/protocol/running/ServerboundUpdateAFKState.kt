package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@SurfNettyPacket("cloud:serverbound:update_afk_state", PacketFlow.SERVERBOUND)
@Serializable
class ServerboundUpdateAFKState(val uuid: @Contextual UUID, val isAfk: Boolean) : NettyPacket()