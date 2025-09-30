package dev.slne.surf.cloud.standalone.spring

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.core.common.spring.CloudSpringApplicationConfiguration
import dev.slne.surf.cloud.standalone.config.StandaloneConfigHolder
import org.springframework.boot.builder.SpringApplicationBuilder

@AutoService(CloudSpringApplicationConfiguration::class)
class FlywayDisabler : CloudSpringApplicationConfiguration {
    override fun configureApplication(builder: SpringApplicationBuilder) {
        if (!StandaloneConfigHolder.isFlywayEnabled()) {
            builder.properties("spring.flyway.enabled=false")
        }
    }
}