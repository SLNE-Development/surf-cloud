package dev.slne.surf.cloud.standalone.plugin.provider

import dev.slne.surf.cloud.api.server.plugin.configuration.PluginMeta
import dev.slne.surf.cloud.api.server.plugin.dependency.DependencyContext
import dev.slne.surf.cloud.standalone.plugin.provider.configuration.LoadOrderConfiguration
import java.nio.file.Path
import java.util.jar.JarFile

interface PluginProvider<T> {
    val source: Path
    val fileName: Path
        get() = source.fileName
    val parentSource: Path
        get() = source.parent

    val file: JarFile
    val meta: PluginMeta

    fun createInstance(): T
    fun createConfiguration(toLoad: Map<String, PluginProvider<*>>): LoadOrderConfiguration

    /**
     * Returns a list of missing dependencies
     */
    fun validateDependencies(context: DependencyContext): Set<String>
}