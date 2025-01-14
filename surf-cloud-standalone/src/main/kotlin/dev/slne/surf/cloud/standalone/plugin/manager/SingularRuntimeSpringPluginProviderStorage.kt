package dev.slne.surf.cloud.standalone.plugin.manager

import dev.slne.surf.cloud.api.server.server.plugin.StandalonePlugin
import dev.slne.surf.cloud.standalone.plugin.entrypoint.dependency.PluginMetaDependencyTree
import dev.slne.surf.cloud.standalone.plugin.entrypoint.Entrypoint
import dev.slne.surf.cloud.standalone.plugin.entrypoint.LaunchEntryPointHandler
import dev.slne.surf.cloud.standalone.plugin.provider.PluginProvider
import dev.slne.surf.cloud.standalone.plugin.provider.impl.StandalonePluginParent
import dev.slne.surf.cloud.standalone.plugin.storage.ServerSpringPluginProviderStorage

class SingularRuntimeSpringPluginProviderStorage(
    private val dependencyTree: PluginMetaDependencyTree
) : ServerSpringPluginProviderStorage() {
    var lastProvider: PluginProvider<StandalonePlugin>? = null
        private set
    var singleLoaded: StandalonePlugin? = null
        private set

    override fun register(provider: PluginProvider<StandalonePlugin>) {
        super.register(provider)
        check(lastProvider == null) { "Plugin registered two StandalonePlugins" }

        if (provider is StandalonePluginParent.StandalonePluginProvider) {
            error("Cannot register standalone plugins during runtime")
        }
        lastProvider = provider
        LaunchEntryPointHandler.register(Entrypoint.SPRING_PLUGIN, provider)
    }

    override suspend fun enter() {
        lastProvider ?: return

        // Go through normal plugin loading logic
        super.enter()
    }

    override suspend fun processProvided(
        provider: PluginProvider<StandalonePlugin>,
        provided: StandalonePlugin
    ) {
        super.processProvided(provider, provided)
        singleLoaded = provided
    }

    override fun throwOnCycle(): Boolean {
        return false
    }

    override fun createDependencyTree(): PluginMetaDependencyTree {
        return dependencyTree
    }
}