@file:Internal
package dev.slne.surf.cloud.core.config

import org.jetbrains.annotations.ApiStatus.Internal
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class SurfCloudConfig(
    @Comment("Config for various connections")
    @Setting("connection")
    val connectionConfig: ConnectionConfig = ConnectionConfig()
)

@ConfigSerializable
data class ConnectionConfig(
    @Comment("Config for database connection")
    @Setting("database")
    val databaseConfig: DatabaseConfig = DatabaseConfig(),

    @Comment("Config for redis connection")
    @Setting("redis")
    val redisConfig: RedisConfig = RedisConfig(),

    @Comment("Config for netty connection")
    @Setting("netty")
    val nettyConfig: NettyConfig = NettyConfig()
)

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
    val password: String = ""
)

@ConfigSerializable
data class RedisConfig(
    @Comment("Host for redis connection")
    @Setting("host")
    val host: String = "127.0.0.1",

    @Comment("Port for redis connection")
    @Setting("port")
    val port: Int = 6379,

    @Comment("Username for redis connection")
    @Setting("username")
    val username: String = "",

    @Comment("Password for redis connection")
    @Setting("password")
    val password: String = ""
)

@ConfigSerializable
data class NettyConfig(
    @Comment("Port for netty connection")
    @Setting("port")
    val port: Int = 5555,

    @Comment("Host for netty connection")
    @Setting("host")
    val host: String = "127.0.0.1",

    @Comment("Reconnect delay for netty connection in seconds")
    @Setting("reconnect-delay")
    val reconnectDelay: Int = 3
)
