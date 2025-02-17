package dev.slne.surf.cloud.standalone.player.db

import dev.slne.surf.cloud.api.server.exposed.columns.charUuid
import dev.slne.surf.cloud.api.server.exposed.columns.inet
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.ZoneOffset

object CloudPlayers : LongIdTable("cloud_player") {
    val uuid = charUuid("uuid").uniqueIndex()
    val lastServer = char("last_server", length = 255).nullable()
    val lastSeen = datetime("last_seen")
        .transform(
            { it.atZone(ZoneOffset.UTC) },
            { it.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime() }
        )
        .nullable()
    val lastIpAddress = inet("last_ip_address").nullable()
}