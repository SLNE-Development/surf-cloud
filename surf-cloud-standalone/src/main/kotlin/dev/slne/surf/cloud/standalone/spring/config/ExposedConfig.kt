package dev.slne.surf.cloud.standalone.spring.config

import org.jetbrains.exposed.spring.autoconfigure.ExposedAutoConfiguration
import org.jetbrains.exposed.sql.DatabaseConfig
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ImportAutoConfiguration(ExposedAutoConfiguration::class)
class ExposedConfig {

    @Bean
    fun databaseConfig() = DatabaseConfig {
        useNestedTransactions = true
    }
}