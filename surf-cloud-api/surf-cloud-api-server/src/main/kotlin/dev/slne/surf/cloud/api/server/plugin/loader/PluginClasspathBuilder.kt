package dev.slne.surf.cloud.api.server.plugin.loader

import dev.slne.surf.cloud.api.server.server.plugin.bootstrap.StandalonePluginProviderContext
import dev.slne.surf.cloud.api.server.server.plugin.loader.library.ClassPathLibrary

interface PluginClasspathBuilder {

    val context: StandalonePluginProviderContext
    fun addLibrary(classPathLibrary: ClassPathLibrary): PluginClasspathBuilder
}