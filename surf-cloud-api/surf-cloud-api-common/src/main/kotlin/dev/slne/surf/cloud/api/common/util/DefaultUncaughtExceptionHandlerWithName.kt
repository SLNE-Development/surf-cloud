package dev.slne.surf.cloud.api.common.util

import com.google.common.flogger.FluentLogger
import java.lang.Thread.UncaughtExceptionHandler

class DefaultUncaughtExceptionHandlerWithName(private val log: FluentLogger) :
    UncaughtExceptionHandler {
    override fun uncaughtException(thread: Thread?, e: Throwable?) {
        log.atSevere()
            .withCause(e)
            .log("Uncaught exception in thread %s: %s", thread?.name, e?.message)
    }
}