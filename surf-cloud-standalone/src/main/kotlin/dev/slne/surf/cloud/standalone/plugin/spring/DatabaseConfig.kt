package dev.slne.surf.cloud.standalone.plugin.spring

import dev.slne.surf.surfapi.core.api.config.createSpongeYmlConfig
import dev.slne.surf.surfapi.core.api.config.surfConfigApi
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.nio.file.Path

@ConfigSerializable
data class DatabaseConfig(
    val user: String = "root",
    val password: String = "root",
    val jdbcUrl: String = "jdbc:mariadb://localhost:3306/plugin",
)

fun createDatabaseConfig(dataDir: Path): DatabaseConfig {
//    val config = dataDir / "database.yaml"
//    if (config.notExists()) {
//        config.createParentDirectories()
//        config.createFile()
//    }

    return surfConfigApi.createSpongeYmlConfig<DatabaseConfig>(dataDir, "database.yaml")
}