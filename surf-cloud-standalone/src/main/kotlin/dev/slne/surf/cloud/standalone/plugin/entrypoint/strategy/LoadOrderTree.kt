@file:Suppress("UnstableApiUsage")

package dev.slne.surf.cloud.standalone.plugin.entrypoint.strategy

import com.google.common.graph.MutableGraph
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import dev.slne.surf.cloud.api.server.plugin.configuration.PluginMeta
import dev.slne.surf.cloud.standalone.plugin.provider.PluginProvider
import dev.slne.surf.cloud.standalone.plugin.provider.configuration.StandalonePluginMeta
import dev.slne.surf.surfapi.core.api.util.logger

class LoadOrderTree(
    private val providerMap: Map<String, PluginProvider<*>>,
    private val graph: MutableGraph<String>
) {

    private val log = logger()

    fun add(provider: PluginProvider<*>) {
        val configuration = provider.createConfiguration(providerMap)

        // Build a validated provider's load order changes
        val name = provider.meta.name
        for (dependency in configuration.loadAfter) {
            if (providerMap.containsKey(dependency)) {
                graph.putEdge(name, dependency)
            }
        }

        for (loadBefore in configuration.loadBefore) {
            if (providerMap.containsKey(loadBefore)) {
                graph.putEdge(loadBefore, name)
            }
        }

        graph.addNode(name) // Make sure load order has at least one node
    }

    fun getLoadOrder() = try {
        graph.topologicalSort().asReversed()
    } catch (_: GraphCycleException) {
        val cycles = JohnsonSimpleCycles(graph).findAndRemoveSimpleCycles()
        val cyclingPlugins = mutableObjectSetOf<String>()
        cycles.forEach(cyclingPlugins::addAll)

        if (cyclingPlugins.any { providerMap[it] != null }) {
            logCycleError(cycles, providerMap)
        }

        // Try again after hopefully having removed all cycles
        try {
            graph.topologicalSort().asReversed()
        } catch (_: GraphCycleException) {
            throw PluginGraphCycleException(cycles)
        }
    }

    private fun logCycleError(
        cycles: List<List<String>>,
        providerMapMirror: Map<String, PluginProvider<*>>
    ) {
        log.atSevere().log("=================================")
        log.atSevere().log("Circular plugin loading detected:")

        for ((i, cycle) in cycles.withIndex()) {
            log.atSevere().log("%s) %s -> %s", i + 1, cycle.joinToString(" -> "), cycle.first())
            for (name in cycle) {
                val provider = providerMapMirror[name] ?: return
                logPluginInfo(provider.meta)
            }
        }

        log.atSevere()
            .log("Please report this to the plugin authors of the first plugin of each loop.")
        log.atSevere().log("=================================")
    }

    private fun logPluginInfo(meta: PluginMeta) {
        if (meta.loadBefore.isNotEmpty()) {
            log.atSevere().log("   %s loadbefore: %s", meta.name, meta.loadBefore)
        }

        if (meta is StandalonePluginMeta) {
            if (meta.loadAfter.isNotEmpty()) {
                log.atSevere().log("   %s loadafter: %s", meta.name, meta.loadAfter)
            }
        }
    }
}