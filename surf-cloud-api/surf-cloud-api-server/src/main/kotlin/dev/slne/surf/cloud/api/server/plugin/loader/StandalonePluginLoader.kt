@file:OptIn(InternalPluginApi::class)

package dev.slne.surf.cloud.api.server.plugin.loader

import dev.slne.surf.cloud.api.server.server.plugin.InternalPluginApi
import org.jetbrains.annotations.ApiStatus

@ApiStatus.OverrideOnly
interface StandalonePluginLoader {
    fun classloader(classpathBuilder: PluginClasspathBuilder)
}