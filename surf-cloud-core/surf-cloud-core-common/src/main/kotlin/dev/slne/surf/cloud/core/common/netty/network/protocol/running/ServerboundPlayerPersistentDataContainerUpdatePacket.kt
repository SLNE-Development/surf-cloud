package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import net.kyori.adventure.nbt.CompoundBinaryTag
import java.util.*

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_PLAYER_PERSISTENT_DATA_CONTAINER_UPDATE,
    PacketFlow.SERVERBOUND
)
class ServerboundPlayerPersistentDataContainerUpdatePacket : NettyPacket {
    companion object {
        val STREAM_CODEC = packetCodec(
            ServerboundPlayerPersistentDataContainerUpdatePacket::write,
            ::ServerboundPlayerPersistentDataContainerUpdatePacket
        )
    }

    val uuid: UUID
    val verificationId: Int
    val nbt: CompoundBinaryTag

    constructor(uuid: UUID, verificationId: Int, nbt: CompoundBinaryTag) {
        this.uuid = uuid
        this.verificationId = verificationId
        this.nbt = nbt
    }

    private constructor(buf: SurfByteBuf) {
        uuid = buf.readUuid()
        verificationId = buf.readInt()
        nbt = buf.readCompoundTag()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(uuid)
        buf.writeInt(verificationId)
        buf.writeCompoundTag(nbt)
    }
}