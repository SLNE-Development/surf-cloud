package dev.slne.surf.cloud.api.common

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.AdviceMode
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * Annotation for defining Surf Cloud applications with specific configurations.
 *
 * Combines multiple Spring features such as scheduling, async processing,
 * transaction management, and entity scanning, with options for JPA and Redis base packages.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@SpringBootApplication(
    exclude = [DataSourceAutoConfiguration::class, RedisRepositoriesAutoConfiguration::class]
)
@EnableScheduling
@EnableAsync(mode = AdviceMode.ASPECTJ)
@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@EntityScan
//@EnableRedisRepositories
@EnableCaching(mode = AdviceMode.ASPECTJ)
@Import(JpaAuditingConfiguration::class)
annotation class SurfCloudApplication

@EnableJpaAuditing
@ConditionalOnClass(name = ["org.springframework.beans.factory.aspectj.AnnotationBeanConfigurerAspect"])
@Profile("server")
class JpaAuditingConfiguration