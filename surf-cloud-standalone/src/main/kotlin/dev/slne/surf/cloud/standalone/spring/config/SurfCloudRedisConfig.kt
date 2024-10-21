package dev.slne.surf.cloud.standalone.spring.config

import com.fasterxml.jackson.databind.ObjectMapper
import dev.slne.surf.cloud.core.config.cloudConfig
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
class SurfCloudRedisConfig {
    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        val redisConfig = cloudConfig.connectionConfig.redisConfig
        val standalone = RedisStandaloneConfiguration()
        val clientConfig = LettuceClientConfiguration.builder()
            //        .useSsl()
            //        .startTls()
            //        .and()
            .clientName("surf-cloud-client")
            .build()

        with(standalone) {
            hostName = redisConfig.host
            port = redisConfig.port
            username = redisConfig.username.ifBlank { null }
            setPassword(redisConfig.password)
        }

        return LettuceConnectionFactory(standalone, clientConfig)
    }

    @Bean
    fun redisSerializer(mapper: ObjectMapper) = GenericJackson2JsonRedisSerializer(mapper)

    @Bean
    fun reactiveRedisTemplate(
        connectionFactory: ReactiveRedisConnectionFactory,
        redisSerializer: GenericJackson2JsonRedisSerializer
    ) = ReactiveRedisTemplate(
        connectionFactory,
        RedisSerializationContext.newSerializationContext<String, Any>(redisSerializer)
            .key(RedisSerializer.string())
            .value(redisSerializer)
            .build()
    )


    //  @Bean
    //  public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    //    return RedisCacheManager.builder(connectionFactory)
    //        .
    //  }
    @Bean
    fun redisCacheManagerBuilderCustomizer() = RedisCacheManagerBuilderCustomizer { builder ->
        builder.cacheDefaults(
            RedisCacheConfiguration.defaultCacheConfig().prefixCacheNameWith("surf-cloud:")
        ).transactionAware()
    }


    @Bean
    fun redisMessageListenerContainer(
        connectionFactory: RedisConnectionFactory
    ) = RedisMessageListenerContainer().apply {
        setConnectionFactory(connectionFactory)
    }
}
