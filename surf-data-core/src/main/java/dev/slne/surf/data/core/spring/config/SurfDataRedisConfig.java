package dev.slne.surf.data.core.spring.config;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.slne.surf.data.core.config.SurfDataConfig;
import dev.slne.surf.data.core.config.SurfDataConfig.ConnectionConfig.RedisConfig;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
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
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class SurfDataRedisConfig {

  @Bean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
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
  public JsonMapper objectMapper(ObjectProvider<Module> modules) {
    return JsonMapper.builder()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
        .configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, true)
        .configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true)
        .configure(Feature.USE_FAST_DOUBLE_PARSER, true)
        .configure(Feature.USE_FAST_BIG_NUMBER_PARSER, true)
        .addModules(modules.orderedStream().toArray(Module[]::new))
        .defaultTimeZone(TimeZone.getDefault())
        .build();
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
