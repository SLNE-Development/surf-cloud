package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentType
import dev.slne.surf.cloud.api.common.player.punishment.type.note.PunishmentNote.PunishmentNoteImpl
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import kotlinx.serialization.Serializable

@SurfNettyPacket(
    "cloud:serverbound:fetch_notes_from_punishment",
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
data class ServerboundFetchNotesFromPunishmentPacket(
    val punishmentId: Long,
    val punishType: PunishmentType
) : RespondingNettyPacket<ClientboundFetchNotesFromPunishmentResponse>(),
    InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.LONG_CODEC,
            ServerboundFetchNotesFromPunishmentPacket::punishmentId,
            PunishmentType.STREAM_CODEC,
            ServerboundFetchNotesFromPunishmentPacket::punishType,
            ::ServerboundFetchNotesFromPunishmentPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleFetchNotesFromPunishment(this)
    }
}

@Serializable
@SurfNettyPacket("cloud:clientbound:fetch_notes_from_punishment_response", PacketFlow.CLIENTBOUND)
data class ClientboundFetchNotesFromPunishmentResponse(val notes: List<PunishmentNoteImpl>) :
    ResponseNettyPacket()