package dev.slne.surf.cloud.core.common.spring

import dev.slne.surf.surfapi.core.api.util.logger

class CloudUncaughtExceptionHandler : Thread.UncaughtExceptionHandler {
    companion object {
        private val log = logger()
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        log.atSevere()
            .withCause(e)
            .log(
                """
                An uncaught exception occurred in thread %s
                Exception type: %s
                Exception message: %s
                """.trimIndent(),
                t.name, e.javaClass.name, e.message
            )
    }
}