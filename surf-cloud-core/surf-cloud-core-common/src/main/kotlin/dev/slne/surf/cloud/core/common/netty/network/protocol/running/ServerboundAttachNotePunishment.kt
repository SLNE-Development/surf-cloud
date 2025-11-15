package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.composite
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentType
import dev.slne.surf.cloud.api.common.player.punishment.type.note.PunishmentNote.PunishmentNoteImpl
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import kotlinx.serialization.Serializable

@SurfNettyPacket(
    "cloud:serverbound:attach_note_punishment",
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
data class ServerboundAttachNoteToPunishmentPacket(
    val punishmentId: Long,
    val note: String,
    val punishType: PunishmentType
) : RespondingNettyPacket<ClientboundAttachedNoteToPunishmentResponse>(),
    InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.LONG_CODEC,
            ServerboundAttachNoteToPunishmentPacket::punishmentId,
            ByteBufCodecs.STRING_CODEC,
            ServerboundAttachNoteToPunishmentPacket::note,
            PunishmentType.STREAM_CODEC,
            ServerboundAttachNoteToPunishmentPacket::punishType,
            ::ServerboundAttachNoteToPunishmentPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleAttachNoteToPunishment(this)
    }
}

@Serializable
@SurfNettyPacket("cloud:clientbound:attached_note_punishment", PacketFlow.CLIENTBOUND)
class ClientboundAttachedNoteToPunishmentResponse(val note: PunishmentNoteImpl) :
    ResponseNettyPacket()