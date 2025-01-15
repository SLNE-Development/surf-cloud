package dev.slne.surf.cloud.api.server.meta

import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Repository

/**
 * Annotation for marking a class as a JPA repository in the Surf Cloud application.
 *
 * @property value The bean name of the repository.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Repository
annotation class SurfJpaRepository(@get:AliasFor(annotation = Repository::class) val value: String = "")
