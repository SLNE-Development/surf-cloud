package dev.slne.surf.cloud.api.server.plugin

import kotlinx.coroutines.CoroutineScope
import org.springframework.aot.hint.annotation.Reflective
import java.lang.annotation.Inherited
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION

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
@Target(CLASS, FUNCTION)
@Retention(RUNTIME)
@MustBeDocumented
@Reflective
@Inherited
annotation class CoroutineTransactional(
    val transactionManager: String = "",
    val transactionIsolation: Int = -1,
    val readOnly: ReadOnly = ReadOnly.DEFAULT,
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

    interface ScopeProvider {
        /**
         * Provides the current coroutine scope for the transaction.
         * This is used to ensure that the transaction is suspended correctly
         * when the coroutine yields.
         */
        fun getCurrentScope(): CoroutineScope
    }
}
