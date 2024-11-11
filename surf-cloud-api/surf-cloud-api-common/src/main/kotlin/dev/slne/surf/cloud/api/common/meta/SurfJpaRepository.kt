package dev.slne.surf.cloud.api.common.meta

import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Repository

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Repository
annotation class SurfJpaRepository(@get:AliasFor(annotation = Repository::class) val value: String = "")
