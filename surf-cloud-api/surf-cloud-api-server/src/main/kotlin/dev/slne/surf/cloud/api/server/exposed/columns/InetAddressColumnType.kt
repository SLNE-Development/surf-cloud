package dev.slne.surf.cloud.api.server.exposed.columns

import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import java.net.InetAddress

class InetAddressColumnType : ColumnType<InetAddress>() {
    override fun sqlType(): String = "INET"
    override fun valueFromDB(value: Any): InetAddress = InetAddress.getByName(value.toString())
}

fun Table.inet(name: String): Column<InetAddress> = registerColumn(name, InetAddressColumnType())
