package dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table

object BanPunishmentTable : AbstractUnpunishableExpirablePunishmentTable("punish_bans") {
    val securityBan = bool("security_ban").default(false)
    val raw = bool("raw").default(false)
}