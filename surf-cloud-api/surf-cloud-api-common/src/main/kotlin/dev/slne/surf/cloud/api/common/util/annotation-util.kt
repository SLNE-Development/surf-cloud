package dev.slne.surf.cloud.api.common.util

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties

private val annotationCache = ConcurrentHashMap<Pair<Annotation, String>, Any?>()

@Suppress("UNCHECKED_CAST")
operator fun <T : Annotation, R : Any> T.getValue(thisRef: Any?, property: KProperty<*>): R {
    val propertyName = property.name
    return annotationCache.computeIfAbsent(this to propertyName) {
        this::class.memberProperties
            .firstOrNull { it.name == propertyName }
            ?.call(this)
    } as? R ?: error("Property '$propertyName' not found or incorrect type.")
}