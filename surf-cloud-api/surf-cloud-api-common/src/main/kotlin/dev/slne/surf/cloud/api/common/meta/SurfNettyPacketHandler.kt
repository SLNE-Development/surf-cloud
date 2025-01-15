package dev.slne.surf.cloud.api.common.meta

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
annotation class SurfNettyPacketHandler 
