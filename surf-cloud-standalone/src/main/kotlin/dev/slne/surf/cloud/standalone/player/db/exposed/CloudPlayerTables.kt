package dev.slne.surf.cloud.standalone.player.db.exposed

import dev.slne.surf.cloud.api.common.config.properties.CloudProperties
import dev.slne.surf.cloud.api.server.exposed.columns.charUuid
import dev.slne.surf.cloud.api.server.exposed.columns.inet
import dev.slne.surf.cloud.api.server.exposed.columns.zonedDateTime
import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongIdTable
import dev.slne.surf.cloud.standalone.player.db.exposed.CloudPlayerTable.uuid
import org.hibernate.mapping.PrimaryKey
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone
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

object CloudPlayerPlaytimesTable : AuditableLongIdTable("cloud_player_playtimes") {
    val serverName = char("server_name", 255)
    val category = varchar("category", 255)
    val durationSeconds = long("duration_seconds").default(0)

    val player = reference("cloud_player_id", CloudPlayerTable)
}
