package dev.slne.surf.cloud.standalone.player.db

import org.jetbrains.exposed.dao.ImmutableEntityClass
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.EntityID

class CloudPlayerNameHistoryEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object :
        ImmutableEntityClass<Long, CloudPlayerNameHistoryEntity>(CloudPlayerNameHistories)

    val name by CloudPlayerNameHistories.name
    val player by CloudPlayerEntity referencedOn CloudPlayerNameHistories.player
}