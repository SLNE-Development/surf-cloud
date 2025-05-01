package dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table

import dev.slne.surf.cloud.api.server.exposed.columns.charUuid
import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import java.util.*

abstract class AbstractPunishmentNoteTable(
    name: String,
    foreignPunishmentTable: AbstractPunishmentTable
) : AuditableLongIdTable(name) {
    val noteId = charUuid("note_id").uniqueIndex().clientDefault { UUID.randomUUID() }
    val punishment =
        reference("punishment_id", foreignPunishmentTable, onDelete = ReferenceOption.CASCADE)
    val note = largeText("note")
    val creatorId = long("creator_id").nullable().default(null)
    val generated = bool("generated").default(false)
}

object BanPunishmentNoteTable : AbstractPunishmentNoteTable("punish_notes_ban", BanPunishmentTable)
object KickPunishmentNoteTable :
    AbstractPunishmentNoteTable("punish_notes_kick", KickPunishmentTable)

object MutePunishmentNoteTable :
    AbstractPunishmentNoteTable("punish_notes_mute", MutePunishmentTable)

object WarnPunishmentNoteTable :
    AbstractPunishmentNoteTable("punish_notes_warn", WarnPunishmentTable)