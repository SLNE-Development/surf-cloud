package dev.slne.surf.cloud.api.util

import com.google.common.flogger.FluentLogger

@Suppress("NOTHING_TO_INLINE") // Caller sensitive
inline fun logger(): FluentLogger = FluentLogger.forEnclosingClass()