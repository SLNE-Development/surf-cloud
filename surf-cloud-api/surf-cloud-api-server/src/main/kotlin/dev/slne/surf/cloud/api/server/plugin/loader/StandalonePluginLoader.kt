package dev.slne.surf.cloud.api.server.plugin.loader

import org.jetbrains.annotations.ApiStatus

@ApiStatus.OverrideOnly
interface StandalonePluginLoader {
    fun classloader(classpathBuilder: PluginClasspathBuilder)
}