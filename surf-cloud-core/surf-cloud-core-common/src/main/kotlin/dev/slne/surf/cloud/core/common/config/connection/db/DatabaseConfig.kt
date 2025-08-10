package dev.slne.surf.cloud.core.common.config.connection.db

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class DatabaseConfig(
    @Comment("URL for database connection. Should be in the format of jdbc:<db_type>://<host>:<port>/<database>")
    @Setting("url")
    val url: String = "jdbc:mariadb://127.0.0.1:3306/surf_data",

    @Comment("Username for database connection")
    @Setting("username")
    val username: String = "root",

    @Comment("Password for database connection")
    @Setting("password")
    val password: String = "",

    @Comment("Type of database to connect to")
    @Setting("type")
    val type: DatabaseType = DatabaseType.MARIADB
) {
    enum class DatabaseType(val driver: String) {
        MYSQL("com.mysql.cj.jdbc.Driver"),
        MARIADB("org.mariadb.jdbc.Driver"),
    }
}