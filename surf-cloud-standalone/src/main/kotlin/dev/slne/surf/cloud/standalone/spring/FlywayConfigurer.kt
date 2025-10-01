package dev.slne.surf.cloud.standalone.spring

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.core.common.spring.CloudSpringApplicationConfiguration
import dev.slne.surf.cloud.standalone.config.StandaloneConfigHolder
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@AutoService(CloudSpringApplicationConfiguration::class)
class FlywayConfigurer : CloudSpringApplicationConfiguration {
    override fun configureApplication(builder: SpringApplicationBuilder) {
        if (!StandaloneConfigHolder.isFlywayEnabled()) {
            builder.properties("spring.flyway.enabled=false")
        }
    }

    @Component
    @Profile("!plugin")
    class Customizer(private val configHolder: StandaloneConfigHolder) :
        FlywayConfigurationCustomizer {
        override fun customize(configuration: FluentConfiguration) {
            configuration.baselineOnMigrate(configHolder.config.connectionConfig.databaseConfig.flyway.baselineOnMigrate)
        }
    }
}