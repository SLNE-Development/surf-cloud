package dev.slne.surf.cloud.standalone.player.db

import org.jetbrains.exposed.dao.ImmutableCachedEntityClass
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.EntityID

class CloudPlayerNameHistoryEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object :
        ImmutableCachedEntityClass<Long, CloudPlayerNameHistoryEntity>(CloudPlayerNameHistories)

    val name by CloudPlayerNameHistories.name
    val player by CloudPlayerEntity referencedOn CloudPlayerNameHistories.player
    val createdAt by CloudPlayerNameHistories.createdAt
}