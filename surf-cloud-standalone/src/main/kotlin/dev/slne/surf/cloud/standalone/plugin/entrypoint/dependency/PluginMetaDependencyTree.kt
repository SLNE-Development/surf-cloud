@file:Suppress("UnstableApiUsage")

package dev.slne.surf.cloud.standalone.plugin.entrypoint.dependency

import com.google.common.graph.GraphBuilder
import com.google.common.graph.Graphs
import com.google.common.graph.MutableGraph
import dev.slne.surf.cloud.api.server.plugin.configuration.PluginMeta
import dev.slne.surf.cloud.api.server.plugin.dependency.DependencyContext
import dev.slne.surf.cloud.standalone.plugin.provider.PluginProvider
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf

abstract class PluginMetaDependencyTree(
    protected val graph: MutableGraph<String> = GraphBuilder.directed().build<String>()
) : DependencyContext {

    protected val dependencies = mutableObjectSetOf<String>()

    fun add(meta: PluginMeta) {
        val name = meta.name

        registerDependencies(name, meta)
        graph.addNode(name)
        dependencies.add(name)
    }

    protected abstract fun registerDependencies(name: String, meta: PluginMeta)

    fun remove(meta: PluginMeta) {
        val name = meta.name

        unregisterDependencies(name, meta)
        graph.removeNode(name)
        dependencies.remove(name)
    }

    protected abstract fun unregisterDependencies(name: String, meta: PluginMeta)

    override fun isTransitiveDependency(
        plugin: PluginMeta,
        dependency: PluginMeta
    ): Boolean {
        val name = plugin.name

        if (graph.nodes().contains(name)) {
            val reachableNodes = Graphs.reachableNodes(graph, name)
            if (reachableNodes.contains(dependency.name)) {
                return true
            }
        }

        return false
    }

    override fun hasDependency(pluginId: String) = dependencies.contains(pluginId)

    fun add(provider: PluginProvider<*>) {
        add(provider.meta)
    }

    fun remove(provider: PluginProvider<*>) {
        remove(provider.meta)
    }

    override fun toString(): String {
        return "PluginMetaDependencyTree(graph=$graph)"
    }
}