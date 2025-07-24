package dev.slne.surf.cloud.api.server.exposed.columns

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.vendors.currentDialect
import java.nio.ByteBuffer

class ComponentColumnType : ColumnType<Component>() {
    override fun sqlType() = currentDialect.dataTypeProvider.largeTextType()

    override fun valueFromDB(value: Any): Component = when (value) {
        is Component -> value
        is String -> GsonComponentSerializer.gson().deserialize(value)
        is ByteArray -> GsonComponentSerializer.gson().deserialize(value.decodeToString())
        is ByteBuffer -> GsonComponentSerializer.gson().deserialize(value.array().decodeToString())
        else -> error("Unexpected value of type Component: $value of ${value::class.qualifiedName}")
    }

    override fun notNullValueToDB(value: Component): Any =
        GsonComponentSerializer.gson().serialize(value)
}

fun Table.component(name: String): Column<Component> = registerColumn(name, ComponentColumnType())