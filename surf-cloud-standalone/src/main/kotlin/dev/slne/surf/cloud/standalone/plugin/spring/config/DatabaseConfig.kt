package dev.slne.surf.cloud.standalone.plugin.spring.config

import dev.slne.surf.cloud.api.server.plugin.configuration.PluginMeta
import dev.slne.surf.cloud.standalone.plugin.PluginInitializerManager
import dev.slne.surf.surfapi.core.api.config.createSpongeYmlConfig
import dev.slne.surf.surfapi.core.api.config.surfConfigApi
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.nio.file.Path
import kotlin.io.path.div

@ConfigSerializable
data class DatabaseConfig(
    val user: String = "root",
    val password: String = "root",
    val jdbcUrl: String = "jdbc:mariadb://localhost:3306/plugin",
    val hikari: DatabaseHikariConfig = DatabaseHikariConfig(),
    val flyway: FlywayConfig = FlywayConfig()
) {
    @ConfigSerializable
    data class FlywayConfig(
        val baselineOnMigrate: Boolean = false,
    )

    @ConfigSerializable
    data class DatabaseHikariConfig(
        val minimumIdle: Int = 10,
        val maximumPoolSize: Int = 10,
        val idleTimeoutMs: Long = 600000,
        val connectionTimeoutMs: Long = 30000,
        val maxLifetimeMs: Long = 1800000,
    )
}

@Configuration(proxyBeanMethods = false)
@Profile("plugin")
class DatabaseConfigConfiguration {

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    fun pluginDatabaseConfig(meta: PluginMeta): DatabaseConfig {
        return createDatabaseConfig(PluginInitializerManager.pluginDirectoryPath / meta.name)
    }
}


fun createDatabaseConfig(dataDir: Path): DatabaseConfig {
//    val config = dataDir / "database.yaml"
//    if (config.notExists()) {
//        config.createParentDirectories()
//        config.createFile()
//    }

    return surfConfigApi.createSpongeYmlConfig<DatabaseConfig>(dataDir, "database.yaml")
}