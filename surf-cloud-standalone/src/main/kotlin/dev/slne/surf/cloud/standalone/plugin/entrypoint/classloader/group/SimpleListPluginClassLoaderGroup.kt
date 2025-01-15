package dev.slne.surf.cloud.standalone.plugin.entrypoint.classloader.group

import dev.slne.surf.cloud.api.server.plugin.provider.classloader.SpringPluginClassloader
import dev.slne.surf.cloud.api.server.plugin.provider.classloader.SpringPluginClassloaderGroup
import java.util.concurrent.CopyOnWriteArrayList

abstract class SimpleListPluginClassLoaderGroup(
    val classloaders: MutableList<SpringPluginClassloader> = CopyOnWriteArrayList()
) : SpringPluginClassloaderGroup {
    override fun classByName(
        name: String,
        resolve: Boolean,
        requester: SpringPluginClassloader
    ): Class<*>? {
        try {
            return lookupClass(name, false, requester) // First check the requester
        } catch (_: ClassNotFoundException) {
        }

        for (classloader in classloaders) {
            try {
                return lookupClass(name, resolve, classloader)
            } catch (_: ClassNotFoundException) {
            }
        }

        return null
    }

    protected open fun lookupClass(
        name: String,
        resolve: Boolean,
        current: SpringPluginClassloader
    ): Class<*> {
        return current.loadClass(name, resolve, false, true)
    }

    override fun remove(plugin: SpringPluginClassloader) {
        classloaders.remove(plugin)
    }

    override fun add(plugin: SpringPluginClassloader) {
        classloaders.add(plugin)
    }

    override fun toString(): String {
        return "SimpleListPluginClassLoaderGroup(classloaders=$classloaders)"
    }
}