package dev.slne.surf.cloud.api.common.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.aop.framework.AopProxyUtils
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.core.MethodIntrospector
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.type.AnnotationMetadata
import org.springframework.stereotype.Component
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionOperations
import org.springframework.util.StopWatch
import java.lang.reflect.Method
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
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

/**
 * Default dispatcher used for coroutine-based transactional execution.
 * Limits the number of concurrent transactional operations to 8,
 * to avoid exhausting the database connection pool.
 */
@PublishedApi
internal val txDispatcher = newSingleThreadContext("txDispatcher").limitedParallelism(1)

/**
 * Coroutine-compatible transactional execution using [TransactionOperations].
 *
 * This function allows executing a transactional [block] within a coroutine,
 * using a coroutine context that is safe and optimized for database operations.
 *
 * The default [context] is a custom limited-parallelism dispatcher (max 8 concurrent operations),
 * which prevents excessive parallel access to the database, avoiding problems like:
 * - connection pool exhaustion (e.g., in HikariCP)
 * - too many open EntityManager instances
 * - heavy database load due to uncontrolled concurrency
 *
 * If needed, you can override the [context], e.g., with [Dispatchers.Default] or [Dispatchers.IO].
 *
 * ### Example:
 * ```
 * val result = transactionTemplate.executeAndAwait {
 *     myRepository.save(...)
 *     "Done"
 * }
 * ```
 *
 * @param context Optional coroutine context; defaults to [txDispatcher], limiting DB concurrency.
 * @param block The transactional code block to run.
 * @return The result of the block.
 */
@OptIn(ExperimentalContracts::class)
@Suppress("WRONG_INVOCATION_KIND", "LEAKED_IN_PLACE_LAMBDA")
suspend inline fun <T> TransactionOperations.executeAndAwait(
    context: CoroutineContext = txDispatcher,
    crossinline block: suspend (TransactionStatus) -> T
): T {
    contract {
        callsInPlace(block, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }

    return withContext(context) {
        execute{
            runBlocking {
                block(it)
            }
        } as T
    }
}


inline fun <T> ObjectProvider<T>.forEachOrdered(action: (T) -> Unit) {
    orderedStream().iterator().forEach(action)
}