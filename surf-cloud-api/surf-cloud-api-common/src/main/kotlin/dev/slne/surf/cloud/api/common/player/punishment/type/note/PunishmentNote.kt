package dev.slne.surf.cloud.api.common.player.punishment.type.note

import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
sealed interface PunishmentNote {
    val noteId: UUID
    val punishmentId: String
    val note: String
    val isBotNote: Boolean

    @Serializable
    @InternalApi
    data class PunishmentNoteImpl(
        override val punishmentId: String,
        override val note: String,
        override val noteId: @Contextual UUID = UUID.randomUUID(),
        override val isBotNote: Boolean,
    ) : PunishmentNote
}