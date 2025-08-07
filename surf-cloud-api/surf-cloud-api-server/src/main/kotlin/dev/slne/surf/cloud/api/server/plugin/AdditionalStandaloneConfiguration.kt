package dev.slne.surf.cloud.api.server.plugin

import dev.slne.surf.cloud.api.server.plugin.utils.PluginUtilProxies
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.suspendCancellableCoroutine
import org.aopalliance.aop.Advice
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.jetbrains.exposed.spring.SpringTransactionManager
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.reactivestreams.Publisher
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.AutoConfigurationPackages
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.*
import org.springframework.core.KotlinDetector
import org.springframework.core.Ordered
import org.springframework.core.PriorityOrdered
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.instrument.classloading.LoadTimeWeaver
import org.springframework.instrument.classloading.SimpleThrowawayClassLoader
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.Instrumentation
import java.lang.reflect.Method
import java.security.ProtectionDomain
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.startCoroutine

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Configuration
@Import(
    CoroutineTransactionalAspect::class,
    LoadTimeWeavingConfiguration::class,
    TransactionConfiguration::class
)
@EnableAsync(mode = AdviceMode.ASPECTJ)
@EnableCaching(mode = AdviceMode.ASPECTJ)
@EnableAutoConfiguration(
    exclude = [
        LiquibaseAutoConfiguration::class,
    ]
)
annotation class AdditionalStandaloneConfiguration

@AdditionalStandaloneConfiguration
class PluginConfig


@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
@Configuration
class TransactionConfiguration

@EnableLoadTimeWeaving(aspectjWeaving = EnableLoadTimeWeaving.AspectJWeaving.ENABLED)
@Configuration
class LoadTimeWeavingConfiguration(context: ApplicationContext) : LoadTimeWeavingConfigurer {
    private val allowedPrefixes = AutoConfigurationPackages.get(context)
        .map { it.trimEnd('.') + "." }
//        .plus(listOf("org.springframework."))

    val weaver = LoadTimeWeaverImpl(
        LoadTimeWeaverImpl.getInstrumentation(),
        context.classLoader ?: error("Application context does not have a class loader"),
        allowedPrefixes
    )

    override fun getLoadTimeWeaver(): LoadTimeWeaver = weaver

    class LoadTimeWeaverImpl(
        private val instrumentation: Instrumentation,
        private val classLoader: ClassLoader,
        private val allowedPrefixes: List<String>
    ) : LoadTimeWeaver {
        private val transformers = CopyOnWriteArrayList<ClassFileTransformer>()

        override fun addTransformer(transformer: ClassFileTransformer) {
            val actualTransformer =
                FilteringClassFileTransformer(transformer, classLoader, allowedPrefixes)
            if (transformers.addIfAbsent(actualTransformer)) {
                instrumentation.addTransformer(actualTransformer)
            }
        }

        override fun getInstrumentableClassLoader(): ClassLoader = classLoader
        override fun getThrowawayClassLoader(): ClassLoader =
            SimpleThrowawayClassLoader(classLoader)

        class FilteringClassFileTransformer(
            private val targetTransformer: ClassFileTransformer,
            private val targetClassLoader: ClassLoader,
            private val allowedPrefixes: List<String>
        ) : ClassFileTransformer {
            override fun transform(
                loader: ClassLoader?,
                className: String?,
                classBeingRedefined: Class<*>?,
                protectionDomain: ProtectionDomain?,
                classfileBuffer: ByteArray?
            ): ByteArray? {
                if (loader != targetClassLoader || className == null) return null
                val dotted = className.replace('/', '.')
                if (allowedPrefixes.none { dotted.startsWith(it) }) {
                    return null
                }

                return try {
                    targetTransformer.transform(
                        loader,
                        className,
                        classBeingRedefined,
                        protectionDomain,
                        classfileBuffer
                    )
                } catch (ex: Throwable) {
                    log.atWarning()
                        .withCause(ex)
                        .log("LTW: skipping weaving for $dotted due to ${ex.javaClass.simpleName}: ${ex.message}")
                    null
                }
            }
        }

        companion object {
            private val log = logger()
            private const val AGENT_CLASS =
                "org.springframework.instrument.InstrumentationSavingAgent"
            private val agentClass: Class<*> by lazy {
                Class.forName(AGENT_CLASS, true, javaClass.classLoader.parent)
            }
            private val agentMethod by lazy {
                agentClass.getDeclaredMethod(
                    "getInstrumentation",
                )
            }

            fun getInstrumentation(): Instrumentation {
                return agentMethod.invoke(null) as Instrumentation
            }
        }

    }
}

/**
 * **Internal** Aspect that wraps every *suspending* call annotated (directly or
 * transitively) with [CoroutineTransactional] in an Exposed
 * `newSuspendedTransaction`.
 *
 * ### Execution order
 * Marked `@Order(Ordered.LOWEST_PRECEDENCE)` so that **every other Spring
 * advice (e.g. `@Transactional`) runs first**.
 * This guarantees the method body we intercept is the *real* business code,
 * not a reactive proxy returned by Spring.
 *
 * ### Core algorithm
 * 1.  Detect a suspending method.
 * 2.  Replace the **last JVM argument** (the original `Continuation`) with one
 *     we control, so we can resume the outer coroutine after the TX finishes.
 * 3.  Start a new suspending Exposed transaction on the *same* coroutine
 *     context.
 *     *Isolation* and *read-only* flags come from the annotation.
 * 4.  Forward the call (`proceed`) inside that transaction.
 * 5.  Handle three possible return shapes:
 *     * immediate value   → resume outer continuation immediately
 *     * `COROUTINE_SUSPENDED` → Kotlin will resume it later
 *     * Reactor `Publisher`  → subscribe and bridge to coroutine via
 *       `awaitFirstOrNull()`
 *
 * ### Thread-local safety
 * Exposed keeps its transaction in coroutine-local state, therefore switching
 * dispatchers inside the service code is *safe* as long as the coroutine
 * `Context` element travels with it (default behaviour of Kotlin coroutines).
 *
 * ### Note for maintainers
 * This aspect is **implementation detail** of the Cloud project.
 * API consumers only need the annotation; they should never depend on this
 * type directly.
 */
@Aspect
@Component
class CoroutineTransactionalAspect(private val context: ApplicationContext) : Advice, PriorityOrdered {

    /**
     * Around-advice for any method or class annotated with
     * [CoroutineTransactional].
     */
    @Suppress("UNCHECKED_CAST")
    @Around("execution(* *(..)) && (@annotation(dev.slne.surf.cloud.api.server.plugin.CoroutineTransactional) || @within(dev.slne.surf.cloud.api.server.plugin.CoroutineTransactional)) && !@annotation(dev.slne.surf.cloud.api.server.plugin.NotTransactional)")
    fun wrapSuspendTx(pjp: ProceedingJoinPoint): Any? {
        val method = (pjp.signature as MethodSignature).method
        // Skip non-suspending functions early.
        if (!KotlinDetector.isSuspendingFunction(method)) return pjp.proceed()

        // Outer continuation = continuation passed in by Kotlin call-site.
        val outerCont = pjp.args.last() as Continuation<Any?>
        val ctxOuter = outerCont.context
        val annotation =
            findAnnotation(method, pjp) ?: error("CoroutineTransactional annotation not found")

        // Build suspending lambda, which encloses the Exposed transaction.
        val job: suspend () -> Any? = {
            newSuspendedTransaction(
                context = ctxOuter, // preserve caller context
                db = PluginUtilProxies.springTransactionManagerProxy.getCurrentDatabase(
                    resolveTxManager(annotation)
                ),
                transactionIsolation = annotation.transactionIsolation.takeIf { it != -1 },
                readOnly = annotation.readOnly.value,
            ) {
                // Actual business invocation inside the transaction
                pjp.proceedSuspend()
            }
        }

        // Start the lambda *without* blocking and wire its completion to outer continuation.
        job.startCoroutine(object : Continuation<Any?> {
            override val context = ctxOuter
            override fun resumeWith(result: Result<Any?>) {
                result.fold(outerCont::resume, outerCont::resumeWithException)
            }
        })

        // Tell Kotlin the original call is now suspended.
        return COROUTINE_SUSPENDED
    }

    /**
     * Bridge `proceed` into a suspending world and deal with possible
     * Spring-reactive wrappers (`Mono`/`Flux`).
     */
    private suspend fun ProceedingJoinPoint.proceedSuspend(): Any? =
        suspendCancellableCoroutine { cont ->
            try {
                val forwarded = args.clone()
                forwarded[forwarded.lastIndex] = cont

                when (val result = proceed(forwarded)) {
                    // Reactive return type – subscribe and await the first element
                    is Publisher<*> -> CoroutineScope(cont.context).launch {
                        try {
                            cont.resume(result.awaitFirstOrNull())
                        } catch (e: Throwable) {
                            cont.resumeWithException(e)
                        }
                    }

                    COROUTINE_SUSPENDED -> {} // Will be resumed by Kotlin
                    else -> cont.resume(result) // Immediate value
                }
            } catch (e: Throwable) {
                cont.resumeWithException(e)
            }
        }

    private fun resolveTxManager(annotation: CoroutineTransactional) =
        if (annotation.transactionManager.isBlank()) {
            context.getBean<SpringTransactionManager>()
        } else {
            context.getBean<SpringTransactionManager>(annotation.transactionManager)
        }

    private fun findAnnotation(method: Method, pjp: ProceedingJoinPoint): CoroutineTransactional? {
        return AnnotatedElementUtils.findMergedAnnotation(
            method,
            CoroutineTransactional::class.java
        ) ?: AnnotatedElementUtils.findMergedAnnotation(
            pjp.target.javaClass,
            CoroutineTransactional::class.java
        )
    }

    override fun getOrder(): Int {
        return Ordered.LOWEST_PRECEDENCE
    }
}