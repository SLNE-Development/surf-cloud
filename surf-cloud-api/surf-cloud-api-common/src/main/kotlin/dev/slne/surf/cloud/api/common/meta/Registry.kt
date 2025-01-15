package dev.slne.surf.cloud.api.common.meta

import org.springframework.core.annotation.AliasFor
import org.springframework.stereotype.Component

/**
 * Annotation for marking a class as a registry component in the Surf Cloud application.
 *
 * @property value The bean name of the registry.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class Registry(@get:AliasFor(annotation = Component::class) val value: String = "")
