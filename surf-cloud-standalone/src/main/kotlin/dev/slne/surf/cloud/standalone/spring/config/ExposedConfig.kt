package dev.slne.surf.cloud.standalone.spring.config

import org.jetbrains.exposed.sql.DatabaseConfig
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ExposedConfig {

    @Bean
    @ConditionalOnClass(DatabaseConfig::class)
    fun databaseConfig() = DatabaseConfig {
        useNestedTransactions = true
    }
}