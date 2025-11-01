package dev.slne.surf.cloud.core.common.player.punishment.type

import dev.slne.surf.cloud.api.common.player.punishment.type.kick.PunishmentKick
import dev.slne.surf.cloud.api.common.player.punishment.type.note.PunishmentNote
import dev.slne.surf.surfapi.core.api.util.toObjectList
import dev.slne.surf.cloud.core.common.messages.MessageManager
import dev.slne.surf.cloud.core.common.player.PunishmentManager
import dev.slne.surf.cloud.core.common.util.bean
import it.unimi.dsi.fastutil.objects.ObjectList
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import org.springframework.core.ResolvableType
import java.time.ZonedDateTime
import java.util.*

@Serializable
data class PunishmentKickImpl(
    val id: Long,
    override val punishmentId: String,
    override val punishedUuid: @Contextual UUID,
    override val issuerUuid: @Contextual UUID?,
    override val reason: String?,

    override val punishmentDate: @Contextual ZonedDateTime = ZonedDateTime.now(),
    override val parent: PunishmentKickImpl? = null,
) : AbstractPunishment(), PunishmentKick {
    override val punishmentUrlReplacer: String = "kicks"

    override fun punishmentPlayerComponent(): Component {
        return MessageManager.Punish.Kick(this).kickDisconnectComponent()
    }

    override suspend fun addNote(note: String): PunishmentNote {
        return bean<PunishmentManager>().attachNoteToKick(id, note)
    }

    override suspend fun fetchNotes(): ObjectList<out PunishmentNote> {
        return bean<PunishmentManager>().fetchNotesForKick(id).toObjectList()
    }

    override fun getResolvableType(): ResolvableType {
        return apiType
    }

    companion object {
        private val apiType = ResolvableType.forClass(PunishmentKick::class.java)
    }
}