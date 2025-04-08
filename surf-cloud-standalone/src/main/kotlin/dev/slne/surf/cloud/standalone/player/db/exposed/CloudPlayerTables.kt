package dev.slne.surf.cloud.standalone.player.db.exposed

import dev.slne.surf.cloud.api.common.config.properties.CloudProperties
import dev.slne.surf.cloud.api.server.exposed.columns.charUuid
import dev.slne.surf.cloud.api.server.exposed.columns.inet
import dev.slne.surf.cloud.api.server.exposed.columns.zonedDateTime
import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongIdTable
import org.jetbrains.exposed.dao.id.LongIdTable
import java.net.Inet4Address

object CloudPlayerTable : AuditableLongIdTable("cloud_player") {
    val uuid = charUuid("uuid").uniqueIndex()
    val lastServer = char("last_server", 255).nullable()
    val lastSeen = zonedDateTime("last_seen").nullable()
    val lastIpAddress = inet("last_ip_address")
        .transform(
            wrap = { it as? Inet4Address ?: error("Invalid Inet4Address: $it") },
            unwrap = { it }
        )
        .nullable()
}

object CloudPlayerNameHistoryTable : AuditableLongIdTable("cloud_player_name_history") {
    val name = char("name", 16)
    val player = reference("cloud_player_id", CloudPlayerTable)
}