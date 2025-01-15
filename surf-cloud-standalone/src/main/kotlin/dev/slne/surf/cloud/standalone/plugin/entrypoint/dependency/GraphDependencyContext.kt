@file:Suppress("UnstableApiUsage")

package dev.slne.surf.cloud.standalone.plugin.entrypoint.dependency

import com.google.common.graph.Graphs
import com.google.common.graph.MutableGraph
import dev.slne.surf.cloud.api.server.plugin.configuration.PluginMeta
import dev.slne.surf.cloud.api.server.plugin.dependency.DependencyContext

class GraphDependencyContext(
    val dependencyGraph: MutableGraph<String>
) : DependencyContext {
    override fun isTransitiveDependency(
        plugin: PluginMeta,
        dependency: PluginMeta
    ): Boolean {
        val name = plugin.name

        if (dependencyGraph.nodes().contains(name)) {
            val reachableNodes = Graphs.reachableNodes(dependencyGraph, name)
            if (reachableNodes.contains(dependency.name)) {
                return true
            }
        }

        return false
    }

    override fun hasDependency(pluginId: String): Boolean {
        return dependencyGraph.nodes().contains(pluginId)
    }

    override fun toString(): String {
        return "GraphDependencyContext(dependencyGraph=$dependencyGraph)"
    }
}