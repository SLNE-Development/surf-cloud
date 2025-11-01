package dev.slne.surf.cloud.standalone.plugin.exception

import java.io.Serial

class UnknownDependencyException(
    missingDependencies: Iterable<String>,
    pluginName: String
) : RuntimeException(
    "Unknown/missing dependency plugins: [${missingDependencies.joinToString()}]. Please download and install these plugins to run '$pluginName'."
) {
    companion object {
        @Serial
        const val serialVersionUID: Long = 1L
    }
}