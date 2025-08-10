package dev.slne.surf.cloud.core.common.config.connection

import dev.slne.surf.cloud.core.common.config.connection.db.DatabaseConfig
import dev.slne.surf.cloud.core.common.config.connection.netty.NettyConfig
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class ConnectionConfig(
    @Comment("Config for database connection")
    @Setting("database")
    val databaseConfig: DatabaseConfig = DatabaseConfig(),

    @Comment("Config for netty connection")
    @Setting("netty")
    val nettyConfig: NettyConfig = NettyConfig()
)