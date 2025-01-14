package dev.slne.surf.cloud.standalone.plugin.entrypoint.classloader.group

import dev.slne.surf.cloud.api.server.server.plugin.provider.classloader.ClassloaderAccess

class GlobalPluginClassLoaderGroup : SimpleListPluginClassLoaderGroup() {
    override val access = ClassloaderAccess { true }

    override fun toString(): String {
        return "GlobalPluginClassLoaderGroup() ${super.toString()}"
    }
}