package dev.slne.surf.data.api.config;

import dev.slne.surf.data.api.meta.SurfRedisRepository;
import dev.slne.surf.data.api.meta.SurfJpaRepository;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.AliasFor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableJpaRepositories(
    includeFilters = @Filter(type = FilterType.ANNOTATION, classes = SurfJpaRepository.class),
    excludeFilters = @Filter(type = FilterType.ANNOTATION, classes = SurfRedisRepository.class),
    repositoryImplementationPostfix = "JpaImpl"
)
public @interface JpaConfig {

  @AliasFor(annotation = EnableJpaRepositories.class)
  String[] value() default {};

  @AliasFor(annotation = EnableJpaRepositories.class)
  String[] basePackages() default {};

  @AliasFor(annotation = EnableJpaRepositories.class)
  Class<?>[] basePackageClasses() default {};
}
