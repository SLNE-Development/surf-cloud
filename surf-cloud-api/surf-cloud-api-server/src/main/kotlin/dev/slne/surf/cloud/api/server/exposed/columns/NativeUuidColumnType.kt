package dev.slne.surf.cloud.api.server.exposed.columns

import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import java.nio.ByteBuffer
import java.util.*

class NativeUuidColumnType : ColumnType<UUID>() {
    @Language("SQL")
    override fun sqlType() = "UUID"

    override fun valueFromDB(value: Any): UUID? = when (value) {
        is UUID -> value
        is String if value.matches(CharUuidColumnType.uuidRegexp) -> UUID.fromString(value)
        is String -> ByteBuffer.wrap(value.toByteArray()).let { UUID(it.long, it.long) }
        is ByteArray -> ByteBuffer.wrap(value).let { UUID(it.long, it.long) }
        is ByteBuffer -> UUID(value.long, value.long)
        else -> error("Unexpected value of type UUID: $value of ${value::class.qualifiedName}")
    }
}

fun Table.nativeUuid(name: String): Column<UUID> =
    registerColumn(name, NativeUuidColumnType())