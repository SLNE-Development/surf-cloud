package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.long.LongResponsePacket
import kotlinx.serialization.Serializable

@SurfNettyPacket(
    "cloud:bidirectional:keep_alive",
    PacketFlow.BIDIRECTIONAL,
    ConnectionProtocol.RUNNING,
    ConnectionProtocol.PRE_RUNNING
)
@Serializable
class KeepAlivePacket(val keepAliveId: Long) : LongResponsePacket()