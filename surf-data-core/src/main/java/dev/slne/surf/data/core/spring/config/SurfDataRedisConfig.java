package dev.slne.surf.data.core.spring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.slne.surf.data.core.config.SurfDataConfig;
import dev.slne.surf.data.core.config.SurfDataConfig.ConnectionConfig.RedisConfig;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class SurfDataRedisConfig {

  @Bean
  public LettuceConnectionFactory redisConnectionFactory(SurfDataConfig surfDataConfig) {
    final RedisConfig redisConfig = surfDataConfig.connectionConfig.redisConfig;
    final RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration();
    final LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
        .useSsl()
        .startTls()
        .and()
        .clientName("surf-data-client")
        .build();

    standalone.setHostName(redisConfig.host);
    standalone.setPort(redisConfig.port);
    standalone.setUsername(redisConfig.username);
    standalone.setPassword(redisConfig.password);

    return new LettuceConnectionFactory(standalone, clientConfig);
  }

  @Bean
  public GenericJackson2JsonRedisSerializer redisSerializer(ObjectMapper mapper) {
    return new GenericJackson2JsonRedisSerializer(mapper);
  }

  @Bean
  public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(
      ReactiveRedisConnectionFactory connectionFactory,
      GenericJackson2JsonRedisSerializer redisSerializer
  ) {
    return new ReactiveRedisTemplate<>(
        connectionFactory,
        RedisSerializationContext.<String, Object>newSerializationContext(redisSerializer)
            .key(RedisSerializer.string())
            .build()
    );
  }

//  @Bean
//  public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
//    return RedisCacheManager.builder(connectionFactory)
//        .
//  }

  @Bean
  public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
    return builder -> builder
        .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
        .prefixCacheNameWith("surf-data:"))
        .transactionAware();
  }

  @Bean
  public RedisMessageListenerContainer redisMessageListenerContainer(
      RedisConnectionFactory connectionFactory
  ) {
    final RedisMessageListenerContainer listenerContainer = new RedisMessageListenerContainer();
    listenerContainer.setConnectionFactory(connectionFactory);

    return listenerContainer;
  }
}
