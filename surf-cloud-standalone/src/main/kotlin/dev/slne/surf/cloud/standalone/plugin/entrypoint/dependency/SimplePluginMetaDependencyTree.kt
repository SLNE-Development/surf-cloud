package dev.slne.surf.cloud.standalone.plugin.entrypoint.dependency

import com.google.common.graph.MutableGraph
import dev.slne.surf.cloud.api.server.server.plugin.configuration.PluginMeta

@Suppress("UnstableApiUsage")
class SimplePluginMetaDependencyTree: PluginMetaDependencyTree {
    constructor() : super()
    constructor(graph: MutableGraph<String>) : super(graph)

    override fun registerDependencies(
        name: String,
        meta: PluginMeta
    ) {
        meta.pluginDependencies.forEach { graph.putEdge(name, it) }
        meta.pluginSoftDependencies.forEach { graph.putEdge(name, it) }
    }

    override fun unregisterDependencies(
        name: String,
        meta: PluginMeta
    ) {
        meta.pluginDependencies.forEach { graph.removeEdge(name, it) }
        meta.pluginSoftDependencies.forEach { graph.removeEdge(name, it) }
    }
}