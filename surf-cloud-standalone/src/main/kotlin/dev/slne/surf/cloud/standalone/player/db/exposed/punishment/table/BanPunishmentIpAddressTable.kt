package dev.slne.surf.cloud.standalone.player.db.exposed.punishment.table

import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object BanPunishmentIpAddressTable: AuditableLongIdTable("punish_ban_ip_addresses") {
    val ipAddress = char("ip_address", 45)
    val punishment = reference("punishment", BanPunishmentTable, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)

    init {
        index("idx_ip_address", false, ipAddress)
    }
}