package dev.slne.surf.cloud.standalone;

import dev.slne.surf.cloud.api.meta.SurfJpaRepository;
import dev.slne.surf.cloud.api.meta.SurfRedisRepository;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@EnableJpaRepositories(
    includeFilters = @Filter(type = FilterType.ANNOTATION, classes = SurfJpaRepository.class),
    excludeFilters = @Filter(type = FilterType.ANNOTATION, classes = SurfRedisRepository.class),
    repositoryImplementationPostfix = "JpaImpl"
)
@EnableRedisRepositories(
    includeFilters = @Filter(type = FilterType.ANNOTATION, classes = SurfRedisRepository.class),
    excludeFilters = @Filter(type = FilterType.ANNOTATION, classes = SurfJpaRepository.class),
    repositoryImplementationPostfix = "RedisImpl"
)
@Configuration
class __AdditionalSpringStandaloneConfiguration {

}
