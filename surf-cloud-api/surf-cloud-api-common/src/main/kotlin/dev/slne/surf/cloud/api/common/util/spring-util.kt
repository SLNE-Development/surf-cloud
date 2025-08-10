package dev.slne.surf.cloud.api.common.util

import org.springframework.aop.framework.AopProxyUtils
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.core.MethodIntrospector
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.core.annotation.AnnotationAwareOrderComparator
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.type.AnnotationMetadata
import org.springframework.util.StopWatch
import java.lang.reflect.Method
import kotlin.reflect.KClass

operator fun <T> ObjectFactory<T>.getValue(thisRef: Any?, property: Any?): T = this.getObject()
operator fun <T> ObjectFactory<T>.getValue(thisRef: Any?, property: Any?, value: T) =
    this.getObject()

fun Any.ultimateTargetClass() = AopProxyUtils.ultimateTargetClass(this).kotlin
inline fun <reified A : Annotation> KClass<*>.isCandidateFor() =
    AnnotationUtils.isCandidateClass(this.java, A::class.java)

inline fun <reified A : Annotation> KClass<*>.containsMethodWithAnnotation() =
    selectFunctions { it.isAnnotated<A>() }.isNotEmpty()

fun KClass<*>.selectFunctions(predicate: (Method) -> Boolean): MutableSet<Method> =
    MethodIntrospector.selectMethods(java, predicate)

inline fun <reified A : Annotation> Method.isAnnotated() =
    AnnotatedElementUtils.isAnnotated(this, A::class.java)

inline fun <reified A : Annotation> KClass<*>.findAnnotation(): A? =
    AnnotationUtils.findAnnotation(this.java, A::class.java)

fun AnnotationMetadata.getFieldValue(
    fieldName: String,
    expectedType: KClass<*>,
    classLoader: ClassLoader
): Any? {
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

inline fun Any.measureWithStopWatch(taskName: String, block: () -> Unit): StopWatch {
    val stopWatch = StopWatch(javaClass.simpleName)
    stopWatch.start(taskName)
    block()
    stopWatch.stop()
    return stopWatch
}

inline fun <R> StopWatch.measure(taskName: String, block: () -> R): R {
    start(taskName)
    val result = block()
    stop()
    return result
}


inline fun <T> ObjectProvider<T>.forEachOrdered(action: (T) -> Unit) {
    orderedStream().iterator().forEach(action)
}

inline fun <T> ObjectProvider<T>.forEachAnnotationOrdered(action: (T) -> Unit) {
    stream().sorted(AnnotationAwareOrderComparator.INSTANCE).iterator().forEach(action)
}