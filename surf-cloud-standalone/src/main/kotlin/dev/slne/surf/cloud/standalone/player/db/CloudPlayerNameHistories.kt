package dev.slne.surf.cloud.standalone.player.db

import dev.slne.surf.cloud.api.common.util.currentUtc
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object CloudPlayerNameHistories : LongIdTable("cloud_player_name_history") {
    val name = char("name", 16)
    val createdAt = datetime("created_at").clientDefault { currentUtc() }

    val player = reference("player", CloudPlayers)
}