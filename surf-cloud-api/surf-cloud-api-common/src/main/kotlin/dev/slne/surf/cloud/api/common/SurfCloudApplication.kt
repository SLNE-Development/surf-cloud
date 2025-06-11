package dev.slne.surf.cloud.api.common

import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter
import org.springframework.boot.autoconfigure.AutoConfigurationPackage
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.TypeExcludeFilter
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.AdviceMode
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import java.lang.annotation.Inherited


/**
 * Annotation for defining Surf Cloud applications with specific configurations.
 *
 * Combines multiple Spring features such as scheduling, async processing,
 * transaction management, and entity scanning, with options for JPA and Redis base packages.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@EnableScheduling
@EnableAsync(mode = AdviceMode.ASPECTJ)
//@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@EntityScan
@EnableCaching(mode = AdviceMode.ASPECTJ)
@AutoConfigurationPackage
@Inherited
@EnableAutoConfiguration
@ComponentScan(
    excludeFilters = [ComponentScan.Filter(
        type = FilterType.CUSTOM,
        classes = [TypeExcludeFilter::class]
    ), ComponentScan.Filter(
        type = FilterType.CUSTOM,
        classes = [AutoConfigurationExcludeFilter::class]
    )]
)
annotation class SurfCloudApplication
