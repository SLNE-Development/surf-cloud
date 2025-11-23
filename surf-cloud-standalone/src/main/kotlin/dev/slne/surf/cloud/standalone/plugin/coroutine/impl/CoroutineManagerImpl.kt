package dev.slne.surf.cloud.standalone.plugin.coroutine.impl

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.server.plugin.StandalonePlugin
import dev.slne.surf.cloud.api.server.plugin.coroutine.CoroutineManager
import dev.slne.surf.cloud.api.server.plugin.coroutine.CoroutineSession
import java.util.concurrent.ConcurrentHashMap

@AutoService(CoroutineManager::class)
class CoroutineManagerImpl : CoroutineManager {
    private val items = ConcurrentHashMap<StandalonePlugin, CoroutineSessionImpl>()

    override fun getCoroutineSession(plugin: StandalonePlugin): CoroutineSession {
        val session = items[plugin]
        checkNotNull(session) { "Coroutine session for plugin ${plugin.meta.name} is not initialized" }

        return session
    }

    override fun setupCoroutineSession(plugin: StandalonePlugin) {
        items.compute(plugin) { _, value ->
            check(value == null) { "Coroutine session for plugin ${plugin.meta.name} is already initialized" }
            CoroutineSessionImpl(plugin)
        }
    }

    override fun disable(plugin: StandalonePlugin) {
        val session = items.remove(plugin)
        checkNotNull(session) { "Coroutine session for plugin ${plugin.meta.name} is not initialized" }

        session.dispose()
    }
}