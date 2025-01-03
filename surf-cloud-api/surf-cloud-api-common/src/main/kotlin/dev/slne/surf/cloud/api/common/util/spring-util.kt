package dev.slne.surf.cloud.api.common.util

import org.springframework.aop.framework.AopProxyUtils
import org.springframework.beans.factory.ObjectFactory
import org.springframework.core.MethodIntrospector
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.type.AnnotationMetadata
import java.lang.reflect.Method
import kotlin.reflect.KClass

operator fun <T> ObjectFactory<T>.getValue(thisRef: Any?, property: Any?): T = this.getObject()
operator fun <T> ObjectFactory<T>.getValue(thisRef: Any?, property: Any?, value: T) = this.getObject()

fun Any.ultimateTargetClass() = AopProxyUtils.ultimateTargetClass(this).kotlin
inline fun <reified A : Annotation> KClass<*>.isCandidateFor() =
    AnnotationUtils.isCandidateClass(this.java, A::class.java)
inline fun <reified A: Annotation> KClass<*>.containsMethodWithAnnotation() =
    selectFunctions { it.isAnnotated<A>() }.isNotEmpty()

fun KClass<*>.selectFunctions(predicate: (Method) -> Boolean): MutableSet<Method> =
    MethodIntrospector.selectMethods(java, predicate)

inline fun <reified A : Annotation> Method.isAnnotated() =
    AnnotatedElementUtils.isAnnotated(this, A::class.java)

inline fun <reified A : Annotation> KClass<*>.findAnnotation(): A? =
    AnnotationUtils.findAnnotation(this.java, A::class.java)

fun AnnotationMetadata.getFieldValue(fieldName: String, expectedType: KClass<*>, classLoader: ClassLoader): Any? {
    return try {
        val clazz = classLoader.loadClass(className)
        val field = clazz.getDeclaredField(fieldName)
        if (expectedType.java.isAssignableFrom(field.type)) {
            field.isAccessible = true
            field.get(null)
        } else null
    } catch (e: Exception) {
        null
    }
}