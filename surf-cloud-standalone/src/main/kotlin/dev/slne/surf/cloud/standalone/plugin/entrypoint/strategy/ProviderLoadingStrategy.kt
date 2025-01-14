package dev.slne.surf.cloud.standalone.plugin.entrypoint.strategy

import dev.slne.surf.cloud.standalone.plugin.entrypoint.dependency.PluginMetaDependencyTree
import dev.slne.surf.cloud.standalone.plugin.provider.PluginProvider
import it.unimi.dsi.fastutil.objects.ObjectList

typealias ProviderPair<P> = Pair<PluginProvider<P>, P>

interface ProviderLoadingStrategy<P> {

    suspend fun loadProviders(
        providers: ObjectList<PluginProvider<P>>,
        dependencyTree: PluginMetaDependencyTree
    ): ObjectList<ProviderPair<P>>
}