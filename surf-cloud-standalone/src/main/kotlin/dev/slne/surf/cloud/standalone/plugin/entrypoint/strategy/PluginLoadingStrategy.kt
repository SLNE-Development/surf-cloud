@file:Suppress("UnstableApiUsage")

package dev.slne.surf.cloud.standalone.plugin.entrypoint.strategy

import com.google.common.collect.Maps
import com.google.common.graph.GraphBuilder
import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.api.common.util.mutableObject2ObjectMapOf
import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.standalone.plugin.entrypoint.dependency.PluginMetaDependencyTree
import dev.slne.surf.cloud.standalone.plugin.exception.UnknownDependencyException
import dev.slne.surf.cloud.standalone.plugin.provider.PluginProvider
import it.unimi.dsi.fastutil.objects.ObjectList

class PluginLoadingStrategy<T>(private val configuration: ProviderConfiguration<T>) :
    ProviderLoadingStrategy<T> {
    private val log = logger()

    override suspend fun loadProviders(
        providers: ObjectList<PluginProvider<T>>,
        dependencyTree: PluginMetaDependencyTree
    ): ObjectList<ProviderPair<T>> {
        val providerMap = mutableObject2ObjectMapOf<String, PluginProviderEntry<T>>()
        val providerMapMirror = Maps.transformValues(providerMap) { it.provider }
        val validatedProviders = mutableObjectListOf<PluginProvider<T>>()

        // Populate provider map
        for (provider in providers) {
            val providerMeta = provider.meta
            val entry = PluginProviderEntry(provider)
            val replacedProvider = providerMap.put(providerMeta.name, entry)
            if (replacedProvider != null) {
                log.atSevere()
                    .log(
                        "Ambiguous provider name '%s' for files '%s' and '%s' in '%s'",
                        providerMeta.name,
                        provider.source,
                        replacedProvider.provider.source,
                        replacedProvider.provider.parentSource
                    )
                configuration.onGenericError(replacedProvider.provider)
            }
        }

        // Populate dependency tree
        for (provider in providers) {
            dependencyTree.add(provider)
        }

        // Validate providers, ensuring all of them have valid dependencies. Removing those who are invalid
        for (provider in providers) {
            val meta = provider.meta

            // Populate missing dependencies to capture if there are multiple missing ones.
            val missingDependencies = provider.validateDependencies(dependencyTree)

            if (missingDependencies.isEmpty()) {
                validatedProviders.add(provider)
            } else {
                log.atSevere()
                    .withCause(UnknownDependencyException(missingDependencies, meta.name))
                    .log("Could not load '%s' in '%s'", provider.source, provider.parentSource)

                // Because the validator is invalid, remove it from the provider map
                providerMap.remove(meta.name)

                // Cleanup plugins that failed to load
                dependencyTree.remove(provider)
                configuration.onGenericError(provider)
            }
        }

        val loadOrderTree = LoadOrderTree(providerMapMirror, GraphBuilder.directed().build())
        // Populate load order tree
        for (validated in validatedProviders) {
            loadOrderTree.add(validated)
        }

        // Reverse the topographic search to let us see which providers we can load first.
        val reversedTopographicSort = loadOrderTree.getLoadOrder()
        val loadedPlugins = mutableObjectListOf<ProviderPair<T>>()
        for (providerIdentifier in reversedTopographicSort) {
            // It's possible that this will be null because the above dependencies for soft/load before aren't validated if they exist.
            // The graph could be MutableGraph<PluginProvider<T>>, but we would have to check if each dependency exists there... just
            // nicer to do it here TBH.
            val retrievedProviderEntry = providerMap[providerIdentifier] ?: continue
            if (retrievedProviderEntry.provided) {
                // OR if this was already provided (most likely from a plugin that already "provides" that dependency)
                // This won't matter since the provided plugin is loaded as a dependency, meaning it should have been loaded correctly anyways
                continue // Skip provider that doesn't exist…
            }
            retrievedProviderEntry.provided = true
            val retrievedProvider = retrievedProviderEntry.provider

            try {
                this.configuration.applyContext(retrievedProvider, dependencyTree)

                if (this.configuration.preloadProvider(retrievedProvider)) {
                    val instance = retrievedProvider.createInstance()
                    if (this.configuration.load(retrievedProvider, instance)) {
                        loadedPlugins.add(ProviderPair(retrievedProvider, instance))
                    }
                }
            } catch (e: Throwable) {
                log.atSevere()
                    .withCause(e)
                    .log(
                        "Could not load plugin '%s' in folder '%s'",
                        retrievedProvider.fileName,
                        retrievedProvider.parentSource
                    )
            }
        }

        return loadedPlugins
    }

    private data class PluginProviderEntry<T>(
        val provider: PluginProvider<T>,
        var provided: Boolean = false
    )
}