@file:Suppress("UnstableApiUsage")

package dev.slne.surf.cloud.standalone.plugin.entrypoint.dependency

import dev.slne.surf.cloud.api.server.server.plugin.configuration.PluginMeta
import dev.slne.surf.cloud.standalone.plugin.provider.configuration.StandalonePluginMeta

class BootstrapMetaDependencyTree: PluginMetaDependencyTree() {
    override fun registerDependencies(
        name: String,
        meta: PluginMeta
    ) {
        check(meta is StandalonePluginMeta) { "Only standalone plugins can have a bootstrapper!" }

        // Build a validated provider's dependencies into the graph
        for (dependency in meta.bootstrapDependencies.keys) {
            graph.putEdge(name, dependency)
        }
    }

    override fun unregisterDependencies(
        name: String,
        meta: PluginMeta
    ) {
        check(meta is StandalonePluginMeta) { "PluginMeta must be a StandalonePluginMeta" }

        // Remove the provider's dependencies from the graph
        for (dependency in meta.bootstrapDependencies.keys) {
            graph.removeEdge(name, dependency)
        }
    }

    override fun toString(): String {
        return "BootstrapMetaDependencyTree() ${super.toString()}"
    }
}