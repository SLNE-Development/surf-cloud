package dev.slne.surf.cloud.standalone.plugin.entrypoint.classloader.group

import dev.slne.surf.cloud.api.server.server.plugin.provider.classloader.ClassloaderAccess
import dev.slne.surf.cloud.api.server.server.plugin.provider.classloader.SpringPluginClassloader

class StaticPluginClassLoaderGroup(
    classloaders: MutableList<SpringPluginClassloader>,
    override val access: ClassloaderAccess,
    val mainClassloaderHolder: SpringPluginClassloader
) : SimpleListPluginClassLoaderGroup(classloaders) {

    override fun toString(): String {
        return "StaticPluginClassLoaderGroup(access=$access) ${super.toString()}"
    }
}