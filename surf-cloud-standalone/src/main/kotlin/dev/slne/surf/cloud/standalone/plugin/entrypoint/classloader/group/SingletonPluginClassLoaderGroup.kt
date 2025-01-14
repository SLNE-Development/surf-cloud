package dev.slne.surf.cloud.standalone.plugin.entrypoint.classloader.group

import dev.slne.surf.cloud.api.server.server.plugin.provider.classloader.ClassloaderAccess
import dev.slne.surf.cloud.api.server.server.plugin.provider.classloader.SpringPluginClassloader
import dev.slne.surf.cloud.api.server.server.plugin.provider.classloader.SpringPluginClassloaderGroup

class SingletonPluginClassLoaderGroup(
    val classloader: SpringPluginClassloader
) : SpringPluginClassloaderGroup {
    override val access: ClassloaderAccess = Access()

    override fun classByName(
        name: String,
        resolve: Boolean,
        requester: SpringPluginClassloader
    ): Class<*>? {
        try {
            return classloader.loadClass(name, resolve, false, true)
        } catch (_: ClassNotFoundException) {
        }

        return null

    }

    override fun add(plugin: SpringPluginClassloader) = Unit
    override fun remove(plugin: SpringPluginClassloader) = Unit

    private inner class Access : ClassloaderAccess {
        override fun canAccess(plugin: SpringPluginClassloader): Boolean {
            return classloader == plugin
        }
    }
}