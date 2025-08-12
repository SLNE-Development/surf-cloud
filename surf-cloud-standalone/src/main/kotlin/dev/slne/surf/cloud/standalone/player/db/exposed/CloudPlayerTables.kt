package dev.slne.surf.cloud.standalone.player.db.exposed

import dev.slne.surf.cloud.api.server.exposed.columns.inet
import dev.slne.surf.cloud.api.server.exposed.columns.nativeUuid
import dev.slne.surf.cloud.api.server.exposed.columns.zonedDateTime
import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import java.net.Inet4Address

object CloudPlayerTable : AuditableLongIdTable("cloud_players") {
    val uuid = nativeUuid("uuid").uniqueIndex()
    val lastServer = char("last_server", 255).nullable()
    val lastSeen = zonedDateTime("last_seen").nullable()
    val lastIpAddress = inet("last_ip_address")
        .transform(
            wrap = { it as? Inet4Address ?: error("Invalid Inet4Address: $it") },
            unwrap = { it }
        )
        .nullable()
}

object CloudPlayerNameHistoryTable : AuditableLongIdTable("cloud_player_name_histories") {
    val name = char("name", 16)
    val player = reference(
        "cloud_player_id",
        CloudPlayerTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
}

object CloudPlayerPlaytimesTable : AuditableLongIdTable("cloud_player_playtimes") {
    val serverName = char("server_name", 255)
    val category = varchar("category", 255)
    val durationSeconds = long("duration_seconds").default(0)

    val player = reference(
        "cloud_player_id", CloudPlayerTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
}
