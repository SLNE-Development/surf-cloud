package dev.slne.surf.cloud.api.server.plugin.provider.classloader

import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import dev.slne.surf.surfapi.core.api.util.requiredService

@InternalApi
interface SpringPluginClassloaderStorage {

    companion object {
        val instance = requiredService<SpringPluginClassloaderStorage>()
    }

    fun registerOpenGroup(classloader: SpringPluginClassloader): SpringPluginClassloaderGroup
    fun registerAccessBackedGroup(
        classloader: SpringPluginClassloader,
        access: ClassloaderAccess
    ): SpringPluginClassloaderGroup

    fun unregisterClassloader(classloader: SpringPluginClassloader)
    fun registerUnsafePlugin(classloader: SpringPluginClassloader): Boolean
}