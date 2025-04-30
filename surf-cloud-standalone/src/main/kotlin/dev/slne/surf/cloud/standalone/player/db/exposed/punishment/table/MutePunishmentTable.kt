package dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table

import dev.slne.surf.cloud.api.server.exposed.columns.zonedDateTime

object MutePunishmentTable: AbstractUnpunishableExpirablePunishmentTable("punish_mutes")