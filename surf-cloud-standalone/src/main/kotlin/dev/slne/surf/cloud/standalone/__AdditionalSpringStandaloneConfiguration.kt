package dev.slne.surf.cloud.standalone

import dev.slne.surf.cloud.api.server.meta.SurfJpaRepository
import dev.slne.surf.cloud.api.server.meta.SurfRedisRepository
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

/**
 * Additional Spring configuration for standalone applications,
 * enabling selective JPA and Redis repository scanning.
 */
@Suppress("ClassName")
@EnableJpaRepositories(
    includeFilters = [ComponentScan.Filter(
        type = FilterType.ANNOTATION,
        classes = [SurfJpaRepository::class]
    )],
    excludeFilters = [ComponentScan.Filter(
        type = FilterType.ANNOTATION,
        classes = [SurfRedisRepository::class]
    )],
    repositoryImplementationPostfix = "JpaImpl"
)
@EnableRedisRepositories(
    includeFilters = [ComponentScan.Filter(
        type = FilterType.ANNOTATION,
        classes = [SurfRedisRepository::class]
    )],
    excludeFilters = [ComponentScan.Filter(
        type = FilterType.ANNOTATION,
        classes = [SurfJpaRepository::class]
    )],
    repositoryImplementationPostfix = "RedisImpl"
)
@Configuration
internal class __AdditionalSpringStandaloneConfiguration 
