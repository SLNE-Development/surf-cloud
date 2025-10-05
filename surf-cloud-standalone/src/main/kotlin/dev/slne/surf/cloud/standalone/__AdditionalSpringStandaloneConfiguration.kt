package dev.slne.surf.cloud.standalone

import dev.slne.surf.cloud.api.server.plugin.AdditionalStandaloneConfiguration
import dev.slne.surf.cloud.api.server.plugin.TransactionConfiguration
import org.jetbrains.exposed.spring.autoconfigure.ExposedAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
import org.springframework.context.annotation.Import

/**
 * Additional Spring configuration for standalone applications,
 * enabling selective JPA and Redis repository scanning.
 */
@Suppress("ClassName")
@AdditionalStandaloneConfiguration
@ImportAutoConfiguration(ExposedAutoConfiguration::class, RedisAutoConfiguration::class)
@Import(TransactionConfiguration::class)
//@EnableAspectJAutoProxy(proxyTargetClass = true)
internal class __AdditionalSpringStandaloneConfiguration
