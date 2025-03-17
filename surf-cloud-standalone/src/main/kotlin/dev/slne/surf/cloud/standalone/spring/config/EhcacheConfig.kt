package dev.slne.surf.cloud.standalone.spring.config

import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.cache.configuration.MutableConfiguration

@Configuration
@EnableCaching
class EhcacheConfig {

    @Bean
    fun cacheCustomizer(): JCacheManagerCustomizer {
        return JCacheManagerCustomizer {
            it.createCache(
                "default-update-timestamps-region",
                MutableConfiguration<Any, Any>()
                    .setStatisticsEnabled(true)
            )

            it.createCache(
                "default-query-results-region",
                MutableConfiguration<Any, Any>()
                    .setStatisticsEnabled(true)
            )
        }
    }
}