package dev.slne.surf.cloud.core.common.netty.network.protocol.running.serverbound

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentType
import dev.slne.surf.cloud.api.common.player.punishment.type.note.PunishmentNote.PunishmentNoteImpl
import kotlinx.serialization.Serializable

@Serializable
@SurfNettyPacket("cloud:serverbound:fetch_notes_from_punishment", PacketFlow.SERVERBOUND)
data class ServerboundFetchNotesFromPunishmentPacket(
    val punishmentId: Long,
    val punishType: PunishmentType
) : RespondingNettyPacket<ClientboundFetchNotesFromPunishmentResponse>()

@Serializable
@SurfNettyPacket("cloud:clientbound:fetch_notes_from_punishment_response", PacketFlow.CLIENTBOUND)
data class ClientboundFetchNotesFromPunishmentResponse(val notes: List<PunishmentNoteImpl>) :
    ResponseNettyPacket()