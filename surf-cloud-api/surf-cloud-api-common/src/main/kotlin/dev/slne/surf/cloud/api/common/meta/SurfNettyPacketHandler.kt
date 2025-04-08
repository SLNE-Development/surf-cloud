package dev.slne.surf.cloud.api.common.meta

import org.springframework.aot.hint.annotation.Reflective

/**
 * Annotation for marking methods in a component as packet handlers.
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(
    AnnotationRetention.RUNTIME
)
@Reflective
annotation class SurfNettyPacketHandler(
    val id: String = ""
)
