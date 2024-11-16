package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.util.codec.ExtraCodecs
import net.kyori.adventure.sound.Sound
import java.util.UUID

@SurfNettyPacket(DefaultIds.CLIENTBOUND_PLAY_SOUND_PACKET, PacketFlow.CLIENTBOUND)
class ClientboundPlaySoundPacket : NettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(ClientboundPlaySoundPacket::write, ::ClientboundPlaySoundPacket)
    }

    val uuid: UUID
    val sound: Sound
    val emitter: Sound.Emitter?
    val x: Double?
    val y: Double?
    val z: Double?

    constructor(uuid: UUID, sound: Sound) {
        this.uuid = uuid
        this.sound = sound
        this.emitter = null
        this.x = null
        this.y = null
        this.z = null
    }

    constructor(uuid: UUID, sound: Sound, emitter: Sound.Emitter) {
        check(emitter == Sound.Emitter.self()) { "Emitter must be self" }

        this.uuid = uuid
        this.sound = sound
        this.emitter = emitter
        this.x = null
        this.y = null
        this.z = null
    }

    constructor(uuid: UUID, sound: Sound, x: Double, y: Double, z: Double) {
        this.uuid = uuid
        this.sound = sound
        this.emitter = null
        this.x = x
        this.y = y
        this.z = z
    }

    private constructor(buf: SurfByteBuf) {
        this.uuid = buf.readUuid()
        this.sound = ExtraCodecs.STREAM_SOUND_CODEC.decode(buf)
        this.emitter = buf.readNullable { buffer ->
            ExtraCodecs.STREAM_EMITTER_SELF_CODEC.decode(buffer)
        }
        this.x = buf.readNullableDouble()
        this.y = buf.readNullableDouble()
        this.z = buf.readNullableDouble()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(uuid)
        ExtraCodecs.STREAM_SOUND_CODEC.encode(buf, sound)
        buf.writeNullable(emitter) { buffer, emitter ->
            ExtraCodecs.STREAM_EMITTER_SELF_CODEC.encode(buffer, emitter)
        }
        buf.writeNullable(x)
        buf.writeNullable(y)
        buf.writeNullable(z)
    }
}