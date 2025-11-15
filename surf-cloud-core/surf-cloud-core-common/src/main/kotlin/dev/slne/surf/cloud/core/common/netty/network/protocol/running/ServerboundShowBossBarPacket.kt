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
    DefaultIds.SERVERBOUND_SHOW_BOSS_BAR_PACKET,
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ServerboundShowBossBarPacket(val uuid: UUID, val bossBar: BossBar) : NettyPacket(),
    InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ServerboundShowBossBarPacket::uuid,
            ByteBufCodecs.BOSS_BAR_CODEC,
            ServerboundShowBossBarPacket::bossBar,
            ::ServerboundShowBossBarPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleShowBossBar(this)
    }
}