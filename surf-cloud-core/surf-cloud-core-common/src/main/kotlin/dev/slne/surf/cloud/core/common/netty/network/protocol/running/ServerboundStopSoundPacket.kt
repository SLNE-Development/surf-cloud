package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import net.kyori.adventure.sound.SoundStop
import java.util.*

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_STOP_SOUND_PACKET,
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ServerboundStopSoundPacket(val uuid: UUID, val soundStop: SoundStop) : NettyPacket(),
    InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ServerboundStopSoundPacket::uuid,
            ByteBufCodecs.SOUND_STOP_CODEC,
            ServerboundStopSoundPacket::soundStop,
            ::ServerboundStopSoundPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleStopSound(this)
    }
}