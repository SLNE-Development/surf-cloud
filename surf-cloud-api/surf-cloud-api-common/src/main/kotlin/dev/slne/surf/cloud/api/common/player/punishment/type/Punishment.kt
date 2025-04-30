package dev.slne.surf.cloud.api.common.player.punishment.type

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.punishment.type.note.PunishmentNote
import it.unimi.dsi.fastutil.objects.ObjectList
import net.kyori.adventure.text.Component
import org.intellij.lang.annotations.Pattern
import org.springframework.core.ResolvableTypeProvider
import java.time.ZonedDateTime
import java.util.*

interface Punishment: ResolvableTypeProvider {
    val type: PunishmentType

    @PunishmentIdPattern
    val punishmentId: String
    val punishedUuid: UUID
    val issuerUuid: UUID?
    val reason: String?
    val punishmentDate: ZonedDateTime

    val adminPanelLink: String

    fun punishedPlayer(): OfflineCloudPlayer
    fun issuerPlayer(): OfflineCloudPlayer?

    fun punishmentPlayerComponent(): Component

    suspend fun addNote(note: String): PunishmentNote
    suspend fun fetchNotes(): ObjectList<out PunishmentNote>
}

@Pattern("[a-zA-Z]+")
annotation class PunishmentIdPattern