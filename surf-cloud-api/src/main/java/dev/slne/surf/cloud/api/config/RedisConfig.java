package dev.slne.surf.cloud.api.config;

import dev.slne.surf.cloud.api.meta.SurfRedisRepository;
import dev.slne.surf.cloud.api.meta.SurfJpaRepository;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.AliasFor;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableRedisRepositories(
    includeFilters = @Filter(type = FilterType.ANNOTATION, classes = SurfRedisRepository.class),
    excludeFilters = @Filter(type = FilterType.ANNOTATION, classes = SurfJpaRepository.class),
    repositoryImplementationPostfix = "RedisImpl"
)
@Configuration
public @interface RedisConfig {

  @AliasFor(annotation = EnableRedisRepositories.class)
  String[] value() default {};

  @AliasFor(annotation = EnableRedisRepositories.class)
  String[] basePackages() default {};

  @AliasFor(annotation = EnableRedisRepositories.class)
  Class<?>[] basePackageClasses() default {};
}
