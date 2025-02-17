package dev.slne.surf.cloud.api.common

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.core.annotation.AliasFor
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * Annotation for defining Surf Cloud applications with specific configurations.
 *
 * Combines multiple Spring features such as scheduling, async processing,
 * transaction management, and entity scanning, with options for JPA and Redis base packages.
 *
 * @property basePackages Base packages to scan for Spring components.
 * @property jpaBasePackages Base packages for JPA entities.
 * @property redisBasePackages Base packages for Redis repositories.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SpringBootApplication(
    exclude = [DataSourceAutoConfiguration::class, RedisRepositoriesAutoConfiguration::class]
)
@EnableScheduling
@EnableAsync
@EnableTransactionManagement
@EnableAspectJAutoProxy
@EntityScan
//@EnableRedisRepositories
@EnableCaching
annotation class SurfCloudApplication(
    @get:AliasFor(
        annotation = SpringBootApplication::class,
        attribute = "scanBasePackages"
    ) val basePackages: Array<String> = [],
    //  @AliasFor(annotation = JpaConfig.class, attribute = "basePackages")
    val jpaBasePackages: Array<String> = [],
    //  @AliasFor(annotation = RedisConfig.class, attribute = "basePackages")
    val redisBasePackages: Array<String> = []
)
