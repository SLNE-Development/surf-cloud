package dev.slne.surf.cloud.core.common.spring.config

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Configuration

@Configuration
@EnableCaching
class CaffeineCacheConfig {

//    @Bean
//    fun caffeineConfig(): Caffeine<Any, Any> {
//        return Caffeine.newBuilder()
//            .expireAfterAccess(10.minutes)
//            .maximumSize(1000)
//    }
}