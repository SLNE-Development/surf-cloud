package dev.slne.surf.cloud.standalone.plugin.spring.config

import com.zaxxer.hikari.HikariDataSource
import dev.slne.surf.cloud.api.server.plugin.configuration.PluginMeta
import dev.slne.surf.cloud.standalone.plugin.PluginInitializerManager
import dev.slne.surf.cloud.standalone.plugin.spring.DatabaseConfig
import dev.slne.surf.cloud.standalone.plugin.spring.createDatabaseConfig
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.jdbc.DatabaseDriver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import kotlin.io.path.div

@Configuration(proxyBeanMethods = false)
@Profile("plugin")
class PluginDatasourceConfiguration {

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    fun jdbcConnectionDetails(pluginMeta: PluginMeta): JdbcConnectionDetails =
        ConfigJdbcConnectionDetails(pluginMeta)

    data class ConfigJdbcConnectionDetails(private val meta: PluginMeta) :
        JdbcConnectionDetails {
        private val config =
            createDatabaseConfig(PluginInitializerManager.pluginDirectoryPath / meta.name)

        override fun getUsername() = config.user
        override fun getPassword() = config.password
        override fun getJdbcUrl() = config.jdbcUrl
    }

    private fun pluginDatabaseSource(
        config: DatabaseConfig, pluginClassLoader: ClassLoader, pluginId: String
    ): HikariDataSource {
        val dataSource = DataSourceBuilder.create(pluginClassLoader)
            .type(HikariDataSource::class.java)
            .driverClassName(DatabaseDriver.fromJdbcUrl(config.jdbcUrl).driverClassName)
            .username(config.user)
            .password(config.password)
            .url(config.jdbcUrl)
            .build()

        dataSource.poolName = "PluginDataSource-$pluginId"
        return dataSource
    }
}