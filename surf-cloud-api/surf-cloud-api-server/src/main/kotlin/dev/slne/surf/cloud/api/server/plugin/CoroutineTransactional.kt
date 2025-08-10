package dev.slne.surf.cloud.api.server.plugin

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.intellij.lang.annotations.Language
import org.springframework.aot.hint.annotation.Reflective
import org.springframework.context.ApplicationContext
import java.lang.annotation.Inherited
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

/**
 * Marks a class or function whose **`suspend`** methods should run inside a
 * coroutine-friendly Exposed transaction handled by
 * `CoroutineTransactionalAspect`.
 *
 * ### Behaviour
 * * At runtime the aspect:
 *   1. Detects a *suspending* call on a class / method annotated with this
 *      annotation.
 *   2. Resolves the desired **`SpringTransactionManager`** (see parameters).
 *   3. Starts an **Exposed `newSuspendedTransaction`** *before*
 *      the business code executes and suspends it correctly when the call
 *      yields.
 * * Non-suspending methods remain untouched and can still use the regular
 *   Spring `@Transactional` interceptor side-by-side.
 *
 * ### Placement
 * * **Class level:** applies to *all* methods (recommended for services that
 *   are 100 % coroutine-based).
 * * **Method level:** overrides / narrows the behaviour for individual
 *   functions if you mix blocking and suspending APIs.
 *
 * ### Parameters
 * @property transactionManager  **Bean name** of the
 *   `SpringTransactionManager` to use.
 *   - *Empty* → the primary `SpringTransactionManager` is used.
 * @property transactionIsolation  Optional JDBC isolation level constant
 *   (see `java.sql.Connection.TRANSACTION_*`).
 *   - `-1` keeps Exposed’s default.
 * @property readOnly  Declares whether the transaction should be marked
 *   read-only.
 *   - `DEFAULT` → use Exposed default.
 */
@Suppress("InjectedReferences")
@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
@MustBeDocumented
@Reflective
@Inherited
annotation class CoroutineTransactional(
    val transactionManager: String = "",
    val transactionIsolation: Int = -1,
    val readOnly: ReadOnly = ReadOnly.DEFAULT,

    val dispatcher: Dispatcher = Dispatcher.IO,
    @Language("spring-bean-name") val dispatcherBean: String = "",
    val dispatcherType: KClass<out CoroutineDispatcher> = CoroutineDispatcher::class,
    val contextProvider: KClass<out CoroutineTxContextProvider> = CoroutineTxContextProvider.Default::class,
) {

    /**
     * Tristate read-only flag to mirror Spring’s semantics while allowing
     * “leave unchanged”.
     */
    enum class ReadOnly(val value: Boolean?) {
        TRUE(true),
        FALSE(false),
        DEFAULT(null);
    }

    enum class Dispatcher { CALLER, IO, DEFAULT, UNCONFINED }

    fun interface CoroutineTxContextProvider {
        fun provide(
            outer: CoroutineContext,
            annotation: CoroutineTransactional,
            ctx: ApplicationContext
        ): CoroutineContext

        class Default : CoroutineTxContextProvider {
            override fun provide(
                outer: CoroutineContext,
                annotation: CoroutineTransactional,
                ctx: ApplicationContext
            ): CoroutineContext {
                val d = resolveDispatcher(annotation, ctx, outer)
                return outer + d
            }

            private fun resolveDispatcher(
                a: CoroutineTransactional,
                ctx: ApplicationContext,
                outer: CoroutineContext
            ): CoroutineDispatcher {
                if (a.dispatcherBean.isNotBlank())
                    return ctx.getBean(a.dispatcherBean, CoroutineDispatcher::class.java)

                if (a.dispatcherType != CoroutineDispatcher::class)
                    return ctx.getBean(a.dispatcherType.java)

                return when (a.dispatcher) {
                    Dispatcher.CALLER ->
                        outer[ContinuationInterceptor] as? CoroutineDispatcher
                            ?: Dispatchers.Default

                    Dispatcher.IO -> Dispatchers.IO
                    Dispatcher.DEFAULT -> Dispatchers.Default
                    Dispatcher.UNCONFINED -> Dispatchers.Unconfined
                }
            }
        }
    }

}

/**
 * Marks a function that should **not** be wrapped in a
 * coroutine-friendly Exposed transaction by the `CoroutineTransactionalAspect`.
 *
 * ### Use Case
 * Use this annotation to exclude specific methods from the
 * `CoroutineTransactional` aspect, especially when you want to control
 * transaction management manually or use a different approach.
 */
@Target(FUNCTION)
@Retention(RUNTIME)
@MustBeDocumented
@Inherited
@Reflective
annotation class NotTransactional
