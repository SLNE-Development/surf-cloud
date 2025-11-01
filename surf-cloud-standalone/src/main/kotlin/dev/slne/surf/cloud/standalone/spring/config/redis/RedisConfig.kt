package dev.slne.surf.cloud.standalone.spring.config.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dev.slne.surf.cloud.api.server.redis.RedisEvent
import dev.slne.surf.cloud.standalone.config.StandaloneConfigHolder
import dev.slne.surf.cloud.standalone.spring.config.redis.serialization.RedisEventDeserializer
import dev.slne.surf.cloud.standalone.spring.config.redis.serialization.RedisEventSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.RedisSerializer

@Configuration(proxyBeanMethods = false)
class RedisConfig {

    @Bean
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    fun lettuceConnectionFactory(configHolder: StandaloneConfigHolder): LettuceConnectionFactory {
        val clientConfig = LettuceClientConfiguration.builder()
            .clientName("surf-cloud-redis-client")
            .build()

        val redis = configHolder.config.connectionConfig.redis
        val config = RedisStandaloneConfiguration(redis.host, redis.port).apply {
            password = RedisPassword.of(redis.password)
        }

        return LettuceConnectionFactory(config, clientConfig)
    }

    @Bean
    fun reactiveRedisTemplate(
        connectionFactory: ReactiveRedisConnectionFactory
    ): ReactiveRedisTemplate<String, RedisEvent> {
        val redisEventModule = SimpleModule().apply {
            addSerializer(RedisEventSerializer())
            addDeserializer(RedisEvent::class.java, RedisEventDeserializer())
        }

        val mapper = ObjectMapper()
            .findAndRegisterModules()
            .registerKotlinModule()
            .registerModule(redisEventModule)

        val serializationContext =
            RedisSerializationContext.newSerializationContext<String, RedisEvent>()
                .key(RedisSerializer.string())
                .value(Jackson2JsonRedisSerializer(mapper, RedisEvent::class.java))
                .hashKey(RedisSerializer.string())
                .hashValue(Jackson2JsonRedisSerializer(mapper, RedisEvent::class.java))
                .build()

        return ReactiveRedisTemplate(connectionFactory, serializationContext)
    }
}