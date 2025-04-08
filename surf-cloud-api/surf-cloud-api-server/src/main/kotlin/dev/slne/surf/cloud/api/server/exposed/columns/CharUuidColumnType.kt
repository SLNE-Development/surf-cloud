package dev.slne.surf.cloud.api.server.exposed.columns

import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import java.nio.ByteBuffer
import java.util.*

class CharUuidColumnType : ColumnType<UUID>() {
    @Language("SQL")
    override fun sqlType(): String = "CHAR(36)"

    override fun valueFromDB(value: Any): UUID? = when {
        value is UUID -> value
        value is ByteArray -> ByteBuffer.wrap(value).let { UUID(it.long, it.long) }
        value is String && value.matches(uuidRegexp) -> UUID.fromString(value)
        value is String -> ByteBuffer.wrap(value.toByteArray()).let { UUID(it.long, it.long) }
        else -> error("Unexpected value of type UUID: $value of ${value::class.qualifiedName}")
    }

    override fun notNullValueToDB(value: UUID): Any = value.toString()
    override fun nonNullValueToString(value: UUID): String = "'$value'"

    companion object {
        private val uuidRegexp =
            Regex(
                "[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}",
                RegexOption.IGNORE_CASE
            )
    }
}

fun Table.charUuid(name: String): Column<UUID> = registerColumn(name, CharUuidColumnType())