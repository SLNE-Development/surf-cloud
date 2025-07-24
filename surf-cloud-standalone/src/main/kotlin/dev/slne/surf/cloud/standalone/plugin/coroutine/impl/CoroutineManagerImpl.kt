package dev.slne.surf.cloud.standalone.plugin.coroutine.impl

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.server.plugin.StandalonePlugin
import dev.slne.surf.cloud.api.server.plugin.coroutine.CoroutineManager
import dev.slne.surf.cloud.api.server.plugin.coroutine.CoroutineSession
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import dev.slne.surf.surfapi.core.api.util.synchronize

@AutoService(CoroutineManager::class)
class CoroutineManagerImpl : CoroutineManager {
    private val items =
        mutableObject2ObjectMapOf<StandalonePlugin, CoroutineSessionImpl>().synchronize()

    override fun getCoroutineSession(plugin: StandalonePlugin): CoroutineSession {
        val session = items[plugin]
        checkNotNull(session) { "Coroutine session for plugin ${plugin.meta.name} is not initialized" }

        return session
    }

    override fun setupCoroutineSession(plugin: StandalonePlugin) {
        check(plugin !in items) { "Coroutine session for plugin ${plugin.meta.name} is already initialized" }
        items[plugin] = CoroutineSessionImpl(plugin)

    }

    override fun disable(plugin: StandalonePlugin) {
        val session = items.remove(plugin)
        checkNotNull(session) { "Coroutine session for plugin ${plugin.meta.name} is not initialized" }

        session.dispose()
    }
}