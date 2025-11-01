package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.util.codec.ExtraCodecs
import net.kyori.adventure.bossbar.BossBar
import java.util.*

@SurfNettyPacket(DefaultIds.SERVERBOUND_SHOW_BOSS_BAR_PACKET, PacketFlow.SERVERBOUND)
class ServerboundShowBossBarPacket : NettyPacket {

    companion object {
        val STREAM_CODEC =
            packetCodec(ServerboundShowBossBarPacket::write, ::ServerboundShowBossBarPacket)
    }

    val uuid: UUID
    val bossBar: BossBar

    constructor(uuid: UUID, bossBar: BossBar) {
        this.uuid = uuid
        this.bossBar = bossBar
    }

    private constructor(buf: SurfByteBuf) {
        this.uuid = buf.readUuid()
        this.bossBar = ExtraCodecs.STREAM_BOSSBAR_CODEC.decode(buf)
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(uuid)
        ExtraCodecs.STREAM_BOSSBAR_CODEC.encode(buf, bossBar)
    }
}