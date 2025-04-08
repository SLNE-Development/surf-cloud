package dev.slne.surf.cloud.core.common.spring.config

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.expireAfterAccess
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.time.Duration.Companion.minutes

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