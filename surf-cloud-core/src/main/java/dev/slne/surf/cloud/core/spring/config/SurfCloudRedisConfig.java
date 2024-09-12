package dev.slne.surf.cloud.core.spring.config;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import dev.slne.surf.cloud.core.config.SurfCloudConfig;
import dev.slne.surf.cloud.core.config.SurfCloudConfig.ConnectionConfig.RedisConfig;
import java.util.TimeZone;
import org.springframework.beans.factory.ObjectProvider;
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
public class SurfCloudRedisConfig {

  @Bean
  public LettuceConnectionFactory redisConnectionFactory(SurfCloudConfig surfCloudConfig) {
    final RedisConfig redisConfig = surfCloudConfig.connectionConfig.redisConfig;
    final RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration();
    final LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
//        .useSsl()
//        .startTls()
//        .and()
        .clientName("surf-cloud-client")
        .build();

    standalone.setHostName(redisConfig.host);
    standalone.setPort(redisConfig.port);
    standalone.setUsername(redisConfig.username.isBlank() ? null : redisConfig.username);
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
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
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
            .value(redisSerializer)
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
            .prefixCacheNameWith("surf-cloud:"))
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
