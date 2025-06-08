package dev.slne.surf.cloud.core.common.netty.network.protocol.login

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecUnitSimple
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_WAIT_FOR_SERVER_TO_START_PACKET,
    PacketFlow.SERVERBOUND,
    ConnectionProtocol.LOGIN
)
object ServerboundWaitForServerToStartPacket : NettyPacket() {
    val STREAM_CODEC = streamCodecUnitSimple(this)
}