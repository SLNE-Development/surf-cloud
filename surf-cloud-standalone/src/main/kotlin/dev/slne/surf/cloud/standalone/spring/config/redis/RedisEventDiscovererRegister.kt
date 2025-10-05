package dev.slne.surf.cloud.standalone.spring.config.redis

import dev.slne.surf.cloud.core.common.spring.CloudChildSpringApplicationConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.stereotype.Component

@Component
class RedisEventDiscovererRegister: CloudChildSpringApplicationConfiguration {
    override fun configureChildApplication(
        builder: SpringApplicationBuilder,
        classLoader: ClassLoader,
        vararg parentClassLoader: ClassLoader
    ) {
        builder.sources(RedisEventDiscoverer::class.java)
    }
}