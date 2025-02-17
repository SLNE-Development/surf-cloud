@file:OptIn(ExperimentalContracts::class)

package dev.slne.surf.cloud.api.common.util

import com.google.common.flogger.FluentLogger
import com.google.common.flogger.LoggingApi
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Suppress("NOTHING_TO_INLINE") // Caller sensitive
inline fun logger(): FluentLogger = FluentLogger.forEnclosingClass()

inline fun <API : LoggingApi<API>> LoggingApi<API>.logIf(
    condition: () -> Boolean,
    logOperation: LoggingApi<API>.() -> Unit
) {
    if (condition()) {
        logOperation()
    }
}