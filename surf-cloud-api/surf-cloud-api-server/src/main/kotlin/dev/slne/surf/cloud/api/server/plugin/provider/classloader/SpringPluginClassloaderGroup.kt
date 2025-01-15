package dev.slne.surf.cloud.api.server.plugin.provider.classloader

import dev.slne.surf.cloud.api.common.util.InternalApi

@InternalApi
interface SpringPluginClassloaderGroup {
    val access: ClassloaderAccess

    fun classByName(name: String, resolve: Boolean, requester: SpringPluginClassloader): Class<*>?

    fun add(plugin: SpringPluginClassloader)
    fun remove(plugin: SpringPluginClassloader)
}