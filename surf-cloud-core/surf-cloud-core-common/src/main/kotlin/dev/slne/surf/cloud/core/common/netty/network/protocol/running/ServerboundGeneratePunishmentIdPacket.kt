package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodecUnitSimple
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.string.StringResponsePacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket

@SurfNettyPacket(
    "cloud:serverbound:generate_punishment_id",
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
object ServerboundGeneratePunishmentIdPacket : StringResponsePacket(),
    InternalNettyPacket<RunningServerPacketListener> {
    val STREAM_CODEC = streamCodecUnitSimple(this)

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleGeneratePunishmentId(this)
    }
}