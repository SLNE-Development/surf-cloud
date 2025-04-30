package dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table

import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongIdTable

object BanPunishmentIpAddressTable: AuditableLongIdTable("punish_ban_ip_addresses") {
    val ipAddress = char("ip_address", 45)
    val punishment = reference("punishment", BanPunishmentTable)
}