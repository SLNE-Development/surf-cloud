package dev.slne.surf.cloud.core.common.netty.network.protocol.running.bidirectional

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@SurfNettyPacket("cloud:bidirectional:silent_disconnect_player", PacketFlow.BIDIRECTIONAL)
@Serializable
class SilentDisconnectPlayerPacket(
    val uuid: @Contextual UUID
) : NettyPacket()