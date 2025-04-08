package dev.slne.surf.cloud.api.common.meta

import org.springframework.aot.hint.annotation.Reflective
import org.springframework.core.annotation.AliasFor
import kotlin.reflect.KClass

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
    @get:AliasFor("classes") val value: Array<KClass<*>> = [],
    @get:AliasFor("values") val classes: Array<KClass<*>> = [],
    val condition: String = "",
    val id: String = ""
)
