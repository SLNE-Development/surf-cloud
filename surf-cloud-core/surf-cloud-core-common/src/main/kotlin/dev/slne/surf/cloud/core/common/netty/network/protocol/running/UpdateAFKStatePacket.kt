package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@SurfNettyPacket("cloud:bidirectional:update_afk_state", PacketFlow.BIDIRECTIONAL)
@Serializable
class UpdateAFKStatePacket(val uuid: @Contextual UUID, val isAfk: Boolean) : NettyPacket()