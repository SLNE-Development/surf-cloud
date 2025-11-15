package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.composite
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import net.kyori.adventure.sound.Sound
import java.util.*

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_PLAY_SOUND_PACKET,
    PacketFlow.CLIENTBOUND,
    handlerMode = PacketHandlerMode.DEFAULT
)
class ClientboundPlaySoundPacket(
    val uuid: UUID,
    val sound: Sound,
    val emitter: Sound.Emitter? = null,
    val x: Double? = null,
    val y: Double? = null,
    val z: Double? = null,
    val permission: String? = null
) : NettyPacket(), InternalNettyPacket<RunningClientPacketListener> {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ClientboundPlaySoundPacket::uuid,
            ByteBufCodecs.SOUND_CODEC,
            ClientboundPlaySoundPacket::sound,
            ByteBufCodecs.SOUND_EMITTER_SELF_CODEC.apply(ByteBufCodecs::nullable),
            ClientboundPlaySoundPacket::emitter,
            ByteBufCodecs.DOUBLE_CODEC.apply(ByteBufCodecs::nullable),
            ClientboundPlaySoundPacket::x,
            ByteBufCodecs.DOUBLE_CODEC.apply(ByteBufCodecs::nullable),
            ClientboundPlaySoundPacket::y,
            ByteBufCodecs.DOUBLE_CODEC.apply(ByteBufCodecs::nullable),
            ClientboundPlaySoundPacket::z,
            ByteBufCodecs.STRING_CODEC.apply(ByteBufCodecs::nullable),
            ClientboundPlaySoundPacket::permission,
            ::ClientboundPlaySoundPacket
        )
    }

    override fun handle(listener: RunningClientPacketListener) {
        listener.handlePlaySound(this)
    }
}