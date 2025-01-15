package dev.slne.surf.cloud.standalone.plugin.storage

import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.api.server.plugin.bootstrap.StandalonePluginBootstrap
import dev.slne.surf.cloud.api.server.plugin.dependency.DependencyContext
import dev.slne.surf.cloud.standalone.plugin.PluginInitializerManager
import dev.slne.surf.cloud.standalone.plugin.bootstrap.PluginBootstrapContextImpl
import dev.slne.surf.cloud.standalone.plugin.entrypoint.dependency.BootstrapMetaDependencyTree
import dev.slne.surf.cloud.standalone.plugin.entrypoint.dependency.DependencyContextHolder
import dev.slne.surf.cloud.standalone.plugin.entrypoint.strategy.PluginLoadingStrategy
import dev.slne.surf.cloud.standalone.plugin.entrypoint.strategy.ProviderConfiguration
import dev.slne.surf.cloud.standalone.plugin.provider.PluginProvider
import dev.slne.surf.cloud.standalone.plugin.provider.ProviderStatus
import dev.slne.surf.cloud.standalone.plugin.provider.ProviderStatusHolder

class BootstrapSpringProviderStorage : SimpleProviderStorage<StandalonePluginBootstrap>(
    PluginLoadingStrategy(object : ProviderConfiguration<StandalonePluginBootstrap> {
        override fun applyContext(
            provider: PluginProvider<StandalonePluginBootstrap>,
            context: DependencyContext
        ) {
            if (provider is DependencyContextHolder) {
                provider.setContext(context)
            }
        }

        override suspend fun load(
            provider: PluginProvider<StandalonePluginBootstrap>,
            provided: StandalonePluginBootstrap
        ): Boolean {
            try {
                val context = PluginBootstrapContextImpl.create(
                    provider,
                    PluginInitializerManager.pluginDirectoryPath
                )
                provided.bootstrap(context)
                return true
            } catch (e: Throwable) {
                log.atSevere()
                    .withCause(e)
                    .log(
                        "Error while bootstrapping plugin '%s'. The plugin will not be loaded",
                        provider.source
                    )
                if (provider is ProviderStatusHolder) {
                    provider.status = ProviderStatus.ERRORED
                }
                return false
            }
        }

        override fun onGenericError(provider: PluginProvider<StandalonePluginBootstrap>) {
            if (provider is ProviderStatusHolder) {
                provider.status = ProviderStatus.ERRORED
            }
        }
    })
) {

    override fun createDependencyTree() = BootstrapMetaDependencyTree()

    override fun toString(): String {
        return "BootstrapSpringProviderStorage() ${super.toString()}"
    }

    companion object {
        @JvmStatic
        private val log = logger()
    }
}