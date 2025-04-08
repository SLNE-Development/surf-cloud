package dev.slne.surf.cloud.api.server.exposed.table

import dev.slne.surf.cloud.api.server.exposed.columns.CurrentZonedDateTime
import dev.slne.surf.cloud.api.server.exposed.columns.zonedDateTime
import dev.slne.surf.surfapi.core.api.util.logger
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import java.time.ZonedDateTime

open class AuditableLongIdTable(name: String = "") : LongIdTable(name) {
    val createdAt = zonedDateTime("created_at").defaultExpression(CurrentZonedDateTime)
    val updatedAt = zonedDateTime("updated_at").defaultExpression(CurrentZonedDateTime)
}

abstract class AuditableLongEntity(id: EntityID<Long>, table: AuditableLongIdTable) :
    LongEntity(id) {
    val createdAt by table.createdAt
    var updatedAt by table.updatedAt
}

abstract class AuditableLongEntityClass<out E : AuditableLongEntity>(table: AuditableLongIdTable) :
    LongEntityClass<E>(table) {
    init {
        EntityHook.subscribe { action ->
            if (action.changeType == EntityChangeType.Updated) {
                try {
                    action.toEntity(this)?.updatedAt = ZonedDateTime.now()
                } catch (_: Exception) {
                    logger().atWarning()
                        .log("Failed to update updatedAt field for entity: ${action.entityClass}")
                }
            }
        }
    }
}