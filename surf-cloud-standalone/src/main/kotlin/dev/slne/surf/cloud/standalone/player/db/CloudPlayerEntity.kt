package dev.slne.surf.cloud.standalone.player.db

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class CloudPlayerEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<CloudPlayerEntity>(CloudPlayers)

    val uuid by CloudPlayers.uuid
    var lastServer by CloudPlayers.lastServer
    var lastSeen by CloudPlayers.lastSeen
    var lastIpAddress by CloudPlayers.lastIpAddress

    val nameHistories by CloudPlayerNameHistoryEntity referrersOn CloudPlayerNameHistories.player orderBy CloudPlayerNameHistories.createdAt
}