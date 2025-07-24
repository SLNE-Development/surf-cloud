package dev.slne.surf.cloud.standalone.plugin.entrypoint

import dev.slne.surf.cloud.standalone.plugin.provider.PluginProvider
import dev.slne.surf.cloud.standalone.plugin.storage.BootstrapSpringProviderStorage
import dev.slne.surf.cloud.standalone.plugin.storage.ProviderStorage
import dev.slne.surf.cloud.standalone.plugin.storage.ServerSpringPluginProviderStorage
import dev.slne.surf.surfapi.core.api.util.mutableObject2BooleanMapOf
import dev.slne.surf.surfapi.core.api.util.object2ObjectMapOf
import it.unimi.dsi.fastutil.objects.Object2ObjectMap

object LaunchEntryPointHandler : EntrypointHandler {

    val storage: Object2ObjectMap<Entrypoint<*>, ProviderStorage<*>> =
        object2ObjectMapOf<Entrypoint<*>, ProviderStorage<*>>(
            Entrypoint.SPRING_PLUGIN_BOOTSTRAPPER to BootstrapSpringProviderStorage(),
            Entrypoint.SPRING_PLUGIN to ServerSpringPluginProviderStorage()
        )
    private val enteredMap = mutableObject2BooleanMapOf<Entrypoint<*>>().apply {
        defaultReturnValue(false)
    }

    suspend fun enterBootstrappers() {
        enter(Entrypoint.SPRING_PLUGIN_BOOTSTRAPPER)
    }

    override fun <T> register(
        entrypoint: Entrypoint<T>,
        provider: PluginProvider<T>
    ) {
        val storage = get(entrypoint) ?: error("No storage for entrypoint $entrypoint")
        storage.register(provider)
    }

    override suspend fun enter(entrypoint: Entrypoint<*>) {
        val storage = get(entrypoint) ?: error("No storage for entrypoint $entrypoint")
        storage.enter()
        enteredMap.put(entrypoint, true)
    }

    fun hasEntered(entrypoint: Entrypoint<*>): Boolean {
        return enteredMap.getBoolean(entrypoint)
    }

    private fun <T> get(entrypoint: Entrypoint<T>): ProviderStorage<T>? {
        @Suppress("UNCHECKED_CAST")
        return storage[entrypoint] as? ProviderStorage<T>
    }
}