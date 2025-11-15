package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import net.kyori.adventure.bossbar.BossBar
import java.util.*

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_SHOW_BOSS_BAR_PACKET,
    PacketFlow.CLIENTBOUND,
    handlerMode = PacketHandlerMode.DEFAULT
)
class ClientboundShowBossBarPacket(val uuid: UUID, val bossBar: BossBar) : NettyPacket(),
    InternalNettyPacket<RunningClientPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ClientboundShowBossBarPacket::uuid,
            ByteBufCodecs.BOSS_BAR_CODEC,
            ClientboundShowBossBarPacket::bossBar,
            ::ClientboundShowBossBarPacket
        )
    }

    override fun handle(listener: RunningClientPacketListener) {
        listener.handleShowBossBar(this)
    }
}