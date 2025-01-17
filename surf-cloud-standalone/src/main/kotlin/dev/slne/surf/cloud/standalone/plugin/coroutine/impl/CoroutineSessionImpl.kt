package dev.slne.surf.cloud.standalone.plugin.coroutine.impl

import dev.slne.surf.cloud.api.server.plugin.StandalonePlugin
import dev.slne.surf.cloud.api.server.plugin.coroutine.CoroutineSession
import dev.slne.surf.cloud.standalone.plugin.coroutine.dispatcher.StandaloneCoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.plus
import kotlin.coroutines.CoroutineContext

class CoroutineSessionImpl(
    private val plugin: StandalonePlugin
) : CoroutineSession {
    override val scope: CoroutineScope
    override val dispatcher: CoroutineContext by lazy { StandaloneCoroutineDispatcher() }

    init {
        val exceptionHandler = CoroutineExceptionHandler { _, e ->
            plugin.logger.error("This is not a good thing! See below for more details: ", e)
        }
        val rootCoroutineScope = CoroutineScope(exceptionHandler)

        scope =
            rootCoroutineScope + SupervisorJob() + dispatcher + CoroutineName("standalone-coroutine-${plugin.meta.name}")
    }

    fun dispose() {
        scope.coroutineContext.cancelChildren()
        scope.cancel()
    }
}