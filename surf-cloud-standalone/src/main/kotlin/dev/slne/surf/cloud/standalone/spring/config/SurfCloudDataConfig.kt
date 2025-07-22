package dev.slne.surf.cloud.standalone.spring.config

import dev.slne.surf.cloud.core.common.config.cloudConfig
import dev.slne.surf.surfapi.core.api.util.logger
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@Configuration
@Import(TransactionAutoConfiguration::class)
class SurfCloudDataConfig {
    companion object {
        private val log = logger()
    }

    @Bean
    fun connectionDetails(): JdbcConnectionDetails {
        val databaseConfig = cloudConfig.connectionConfig.databaseConfig

        return object : JdbcConnectionDetails {
            override fun getUsername(): String? {
                return databaseConfig.username
            }

            override fun getPassword(): String? {
                return databaseConfig.password
            }

            override fun getJdbcUrl(): String? {
                return databaseConfig.url
            }
        }
    }
}
