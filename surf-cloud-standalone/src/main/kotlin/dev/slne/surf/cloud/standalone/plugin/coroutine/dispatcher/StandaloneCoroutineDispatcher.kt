package dev.slne.surf.cloud.standalone.plugin.coroutine.dispatcher

import dev.slne.surf.cloud.api.common.util.threadFactory
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext


class StandaloneCoroutineDispatcher : CoroutineDispatcher() {
    private val log = logger()
    private val thread = Executors.newSingleThreadExecutor(threadFactory {
        nameFormat("standalone-coroutine-thread-%d")
        daemon(false)
        uncaughtExceptionHandler { t, e ->
            log.atSevere()
                .withCause(e)
                .log("Uncaught exception in coroutine thread %s", t.name)
        }
    })

    override fun dispatch(
        context: CoroutineContext,
        block: Runnable
    ) {
        thread.execute(block)
    }
}