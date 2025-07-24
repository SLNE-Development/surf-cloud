@file:Suppress("UnstableApiUsage")

package dev.slne.surf.cloud.standalone.plugin.entrypoint.strategy

import com.google.common.graph.Graph
import dev.slne.surf.surfapi.core.api.util.mutableObject2IntMapOf
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf
import it.unimi.dsi.fastutil.objects.ObjectList

fun <N : Any> Graph<N>.topologicalSort(): ObjectList<N> {
    val sorted = mutableObjectListOf<N>()
    val roots = ArrayDeque<N>()
    val nonRoots = mutableObject2IntMapOf<N>()

    for (node in nodes()) {
        // Is a node being referred to by any other nodes?
        val degree = inDegree(node)
        if (degree == 0) {
            // Is a root
            roots.add(node)
        } else {
            // Isn't a root; the number represents how many nodes connect to it.
            nonRoots.put(node, degree)
        }
    }

    // Pick from nodes that aren't referred to anywhere else
    while (true) {
        val next = roots.removeFirstOrNull() ?: break
        for (successor in successors(next)) {
            // Traverse through, moving down a degree
            val newInDegree = nonRoots.removeInt(successor) - 1
            if (newInDegree == 0) {
                roots.add(successor)
            } else {
                nonRoots.put(successor, newInDegree)
            }
        }

        sorted.add(next)
    }

    if (nonRoots.isNotEmpty()) {
        throw GraphCycleException()
    }

    return sorted
}

class GraphCycleException() : RuntimeException()