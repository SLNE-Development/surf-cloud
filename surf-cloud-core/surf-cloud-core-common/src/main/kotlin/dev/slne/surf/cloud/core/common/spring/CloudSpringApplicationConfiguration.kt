package dev.slne.surf.cloud.core.common.spring

import net.kyori.adventure.util.Services
import org.springframework.boot.builder.SpringApplicationBuilder

interface CloudSpringApplicationConfiguration {

    fun configureApplication(
        builder: SpringApplicationBuilder
    )

    companion object {
        val configurations: Set<CloudSpringApplicationConfiguration> =
            Services.services(CloudSpringApplicationConfiguration::class.java)
    }
}