package dev.slne.surf.cloud.standalone.plugin.exception

class UnknownDependencyException(
    missingDependencies: Iterable<String>,
    pluginName: String
) : RuntimeException(
    "Unknown/missing dependency plugins: [${missingDependencies.joinToString()}]. Please download and install these plugins to run '$pluginName'."
)