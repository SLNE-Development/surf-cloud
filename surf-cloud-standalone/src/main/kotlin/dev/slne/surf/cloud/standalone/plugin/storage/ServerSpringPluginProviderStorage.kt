package dev.slne.surf.cloud.standalone.plugin.storage

import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.api.server.plugin.PluginManager
import dev.slne.surf.cloud.api.server.plugin.StandalonePlugin
import dev.slne.surf.cloud.api.server.plugin.dependency.DependencyContext
import dev.slne.surf.cloud.standalone.plugin.entrypoint.dependency.DependencyContextHolder
import dev.slne.surf.cloud.standalone.plugin.entrypoint.strategy.ProviderConfiguration
import dev.slne.surf.cloud.standalone.plugin.manager.SpringPluginManagerImpl
import dev.slne.surf.cloud.standalone.plugin.provider.PluginProvider
import dev.slne.surf.cloud.standalone.plugin.provider.impl.StandalonePluginParent
import it.unimi.dsi.fastutil.objects.ObjectList

open class ServerSpringPluginProviderStorage :
    ConfiguredProviderStorage<StandalonePlugin>(object : ProviderConfiguration<StandalonePlugin> {
        override fun applyContext(
            provider: PluginProvider<StandalonePlugin>,
            context: DependencyContext
        ) {
            val alreadyLoadedPlugin = PluginManager.instance.getPlugin(provider.meta.name)
            check(alreadyLoadedPlugin == null) { "Provider $provider attempted to add duplicate plugin identifier $alreadyLoadedPlugin This will create bugs!" }

            if (provider is DependencyContextHolder) {
                provider.setContext(context)
            }
        }

        override suspend fun load(
            provider: PluginProvider<StandalonePlugin>,
            provided: StandalonePlugin
        ): Boolean {
            // Add it to the map here, we have to run the actual loading logic later.
            (PluginManager.instance as SpringPluginManagerImpl).loadPlugin(provided)
            return true
        }

    }) {
    private val log = logger()

    override fun filterLoadingProviders(provides: ObjectList<PluginProvider<StandalonePlugin>>) {
        provides.removeIf { it is StandalonePluginParent.StandalonePluginProvider && it.shouldSkipCreation() }
    }

    // We need to call the load methods AFTER all plugins are constructed
    override suspend fun processProvided(
        provider: PluginProvider<StandalonePlugin>,
        provided: StandalonePlugin
    ) {
        try {
            log.atInfo().log("Loading server plugin ${provider.meta.displayName}...")
            provided.load()
        } catch (e: Throwable) {
            provided.logger.error(
                "Error initializing plugin '${provider.fileName}' in folder '${provider.parentSource}' (Is it up to date?)",
                e
            )
        }
    }

    override fun toString(): String {
        return "ServerSpringPluginProviderStorage() ${super.toString()}"
    }
}