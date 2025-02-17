package dev.slne.surf.cloud.core.common.spring.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class CaffeineCacheConfig {

    @Bean
    fun caffeineConfig(): Caffeine<Any, Any> {

    }

    @Bean
    @Primary
    fun cacheManager(caffeine: Caffeine<Any, Any>): CacheManager {
        org.springframework.cache.caffeine
    }
}