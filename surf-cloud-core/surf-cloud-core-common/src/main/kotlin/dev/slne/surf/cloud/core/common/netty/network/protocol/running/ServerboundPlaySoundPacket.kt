package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs.DOUBLE_CODEC
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs.SOUND_CODEC
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs.SOUND_EMITTER_SELF_CODEC
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs.STRING_CODEC
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs.UUID_CODEC
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.composite
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.checkEncoded
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import net.kyori.adventure.sound.Sound
import java.util.*

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_PLAY_SOUND_PACKET,
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ServerboundPlaySoundPacket private constructor(
    val uuid: UUID,
    val sound: Sound,
    val emitter: Sound.Emitter?,
    val x: Double?,
    val y: Double?,
    val z: Double?,
    val permission: String?
) : NettyPacket(), InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            UUID_CODEC,
            ServerboundPlaySoundPacket::uuid,
            SOUND_CODEC,
            ServerboundPlaySoundPacket::sound,
            SOUND_EMITTER_SELF_CODEC.apply(ByteBufCodecs::nullable),
            ServerboundPlaySoundPacket::emitter,
            DOUBLE_CODEC.apply(ByteBufCodecs::nullable),
            ServerboundPlaySoundPacket::x,
            DOUBLE_CODEC.apply(ByteBufCodecs::nullable),
            ServerboundPlaySoundPacket::y,
            DOUBLE_CODEC.apply(ByteBufCodecs::nullable),
            ServerboundPlaySoundPacket::z,
            STRING_CODEC.apply(ByteBufCodecs::nullable),
            ServerboundPlaySoundPacket::permission,
            ::ServerboundPlaySoundPacket
        )
    }

    constructor(uuid: UUID, sound: Sound, permission: String? = null) : this(
        uuid,
        sound,
        null,
        null,
        null,
        null,
        permission
    )

    constructor(
        uuid: UUID,
        sound: Sound,
        emitter: Sound.Emitter,
        permission: String? = null
    ) : this(uuid, sound, emitter, null, null, null, permission) {
        checkEncoded(emitter == Sound.Emitter.self()) { "Emitter must be self" }
    }

    constructor(
        uuid: UUID,
        sound: Sound,
        x: Double,
        y: Double,
        z: Double,
        permission: String? = null
    ) : this(uuid, sound, null, x, y, z, permission)

    override fun handle(listener: RunningServerPacketListener) {
        listener.handlePlaySound(this)
    }
}