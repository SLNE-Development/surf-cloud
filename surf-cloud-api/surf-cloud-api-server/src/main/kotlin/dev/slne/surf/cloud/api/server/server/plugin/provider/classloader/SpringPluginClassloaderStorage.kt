package dev.slne.surf.cloud.api.server.server.plugin.provider.classloader

import dev.slne.surf.cloud.api.common.util.requiredService
import dev.slne.surf.cloud.api.server.server.plugin.InternalPluginApi

@InternalPluginApi
interface SpringPluginClassloaderStorage {

    companion object {
        val instance = requiredService<SpringPluginClassloaderStorage>()
    }

    fun registerOpenGroup(classloader: SpringPluginClassloader): SpringPluginClassloaderGroup
    fun registerAccessBackedGroup(classloader: SpringPluginClassloader, access: ClassloaderAccess): SpringPluginClassloaderGroup
    fun unregisterClassloader(classloader: SpringPluginClassloader)
    fun registerUnsafePlugin(classloader: SpringPluginClassloader): Boolean
}