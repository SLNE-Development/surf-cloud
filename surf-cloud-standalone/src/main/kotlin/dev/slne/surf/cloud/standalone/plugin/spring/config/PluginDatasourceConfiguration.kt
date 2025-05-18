package dev.slne.surf.cloud.standalone.plugin.spring.config

import com.zaxxer.hikari.HikariDataSource
import dev.slne.surf.cloud.api.server.plugin.provider.classloader.SpringPluginClassloader
import dev.slne.surf.cloud.standalone.plugin.entrypoint.classloader.SpringPluginClassloaderImpl
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.jdbc.DatabaseDriver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration(proxyBeanMethods = false)
@Profile("plugin")
class PluginDatasourceConfiguration {

    @Bean
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    fun jdbcConnectionDetails(databaseConfig: DatabaseConfig): JdbcConnectionDetails =
        ConfigJdbcConnectionDetails(databaseConfig)

    @Bean
    @Primary
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    fun customizeHikariDataSource(
        databaseConfig: DatabaseConfig,
        pluginClassloader: SpringPluginClassloader
    ): HikariDataSource {
        require(pluginClassloader is SpringPluginClassloaderImpl)
        val datasource =
            pluginDatabaseSource(databaseConfig, pluginClassloader, pluginClassloader.meta.name)
        val config = databaseConfig.hikari

        with(datasource) {
            minimumIdle = config.minimumIdle
            maximumPoolSize = config.maximumPoolSize
            idleTimeout = config.idleTimeoutMs
            connectionTimeout = config.connectionTimeoutMs
            maxLifetime = config.maxLifetimeMs
        }

        return datasource
    }

    data class ConfigJdbcConnectionDetails(private val config: DatabaseConfig) :
        JdbcConnectionDetails {
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