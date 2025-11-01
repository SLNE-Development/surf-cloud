package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.util.codec.ExtraCodecs
import net.kyori.adventure.sound.SoundStop
import java.util.*

@SurfNettyPacket(DefaultIds.CLIENTBOUND_STOP_SOUND_PACKET, PacketFlow.CLIENTBOUND)
class ClientboundStopSoundPacket : NettyPacket {

    companion object {
        val STREAM_CODEC =
            packetCodec(ClientboundStopSoundPacket::write, ::ClientboundStopSoundPacket)
    }

    val uuid: UUID
    val soundStop: SoundStop

    constructor(uuid: UUID, soundStop: SoundStop) {
        this.uuid = uuid
        this.soundStop = soundStop
    }

    private constructor(buf: SurfByteBuf) {
        this.uuid = buf.readUuid()
        this.soundStop = ExtraCodecs.STREAM_SOUND_STOP_CODEC.decode(buf)
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(uuid)
        ExtraCodecs.STREAM_SOUND_STOP_CODEC.encode(buf, soundStop)
    }
}