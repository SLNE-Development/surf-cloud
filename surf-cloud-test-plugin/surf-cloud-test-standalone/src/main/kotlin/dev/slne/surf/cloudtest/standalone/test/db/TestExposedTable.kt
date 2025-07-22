package dev.slne.surf.cloudtest.standalone.test.db

import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongEntity
import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongEntityClass
import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongIdTable
import org.jetbrains.exposed.dao.id.EntityID

object TestExposedTable : AuditableLongIdTable("test_exposed") {
    val name = varchar("name", 255)
    val description = text("description").nullable()
}

class TestExposedEntity(id: EntityID<Long>) : AuditableLongEntity(id, TestExposedTable) {
    companion object : AuditableLongEntityClass<TestExposedEntity>(TestExposedTable)

    var name by TestExposedTable.name
    var description by TestExposedTable.description
}