package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentType
import dev.slne.surf.cloud.api.common.player.punishment.type.note.PunishmentNote
import dev.slne.surf.cloud.api.common.player.punishment.type.note.PunishmentNote.PunishmentNoteImpl
import kotlinx.serialization.Serializable

@Serializable
@SurfNettyPacket("cloud:serverbound:attach_note_punishment", PacketFlow.SERVERBOUND)
data class ServerboundAttachNoteToPunishmentPacket(
    val punishmentId: Long,
    val note: String,
    val punishType: PunishmentType
): RespondingNettyPacket<ClientboundAttachedNoteToPunishmentResponse>()

@Serializable
@SurfNettyPacket("cloud:clientbound:attached_note_punishment", PacketFlow.CLIENTBOUND)
class ClientboundAttachedNoteToPunishmentResponse(val note: PunishmentNoteImpl): ResponseNettyPacket()