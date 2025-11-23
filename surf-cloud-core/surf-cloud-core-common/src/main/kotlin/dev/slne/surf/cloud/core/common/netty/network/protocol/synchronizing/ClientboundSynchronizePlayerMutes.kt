package dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import dev.slne.surf.cloud.core.common.player.punishment.type.PunishmentMuteImpl
import java.util.*

@SurfNettyPacket(
    "cloud:clientbound:synchronize_punishments/mutes",
    PacketFlow.CLIENTBOUND,
    ConnectionProtocol.SYNCHRONIZING,
    handlerMode = PacketHandlerMode.NETTY
)
class ClientboundSynchronizePlayerMutes(
    val playerUuid: UUID,
    val mutes: MutableList<PunishmentMuteImpl>
) : NettyPacket(), InternalNettyPacket<ClientSynchronizingPacketListener> {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ClientboundSynchronizePlayerMutes::playerUuid,
            PunishmentMuteImpl.STREAM_CODEC.apply(ByteBufCodecs.list()),
            ClientboundSynchronizePlayerMutes::mutes,
            ::ClientboundSynchronizePlayerMutes
        )
    }

    override fun handle(listener: ClientSynchronizingPacketListener) {
        listener.handleSynchronizePlayerMutes(this)
    }
}