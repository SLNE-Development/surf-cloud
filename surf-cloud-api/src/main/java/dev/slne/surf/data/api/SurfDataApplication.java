package dev.slne.surf.data.api;

import dev.slne.surf.data.api.config.JpaConfig;
import dev.slne.surf.data.api.config.RedisConfig;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.annotation.AliasFor;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class})
@EnableScheduling
@EnableAsync
@EnableTransactionManagement
@EnableAspectJAutoProxy
@JpaConfig
@RedisConfig
@EntityScan
//@EnableRedisRepositories
public @interface SurfDataApplication {

  @AliasFor(annotation = SpringBootApplication.class, attribute = "scanBasePackages")
  String[] basePackages() default {};

//  @AliasFor(annotation = EnableJpaRepositories.class, attribute = "basePackageClasses")
//  Class<?>[] jpaRepositoriesPackages() default {};

//  @AliasFor(annotation = EnableRedisRepositories.class, attribute = "basePackageClasses")
//  Class<?>[] redisRepositoriesPackages() default {};

//  @AliasFor(annotation = JpaConfig.class, attribute = "basePackages")
  String[] jpaBasePackages() default {};

//  @AliasFor(annotation = RedisConfig.class, attribute = "basePackages")
  String[] redisBasePackages() default {};
}
