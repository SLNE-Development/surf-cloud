package dev.slne.surf.cloud.standalone.player.db.exposed.whitelist

import dev.slne.surf.cloud.api.server.exposed.columns.nativeUuid
import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongIdTable
import dev.slne.surf.cloud.standalone.player.db.exposed.CloudPlayerTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or

object WhitelistTable : AuditableLongIdTable("whitelist") {
    val uuid = nativeUuid("uuid").index()
    val blocked = bool("blocked").default(false)
    val group = varchar("group", 255).nullable()
    val serverName = varchar("server_name", 255).nullable()
    val cloudPlayerId = reference("cloud_player_id", CloudPlayerTable)

    init {
        check("ck_group_xor_server") {
            (group.isNotNull() and serverName.isNull()) or (group.isNull() and serverName.isNotNull())
        }

        uniqueIndex(
            "uniq_whitelist_uuid_group",
            uuid,
            group,
            filterCondition = { group.isNotNull() }
        )

        uniqueIndex(
            "uniq_whitelist_uuid_server",
            uuid,
            serverName,
            filterCondition = { serverName.isNotNull() }
        )
    }
}