package dev.slne.surf.cloud.api.server.plugin.provider.classloader

import dev.slne.surf.cloud.api.server.server.plugin.InternalPluginApi

@InternalPluginApi
fun interface ClassloaderAccess {

    fun canAccess(plugin: SpringPluginClassloader): Boolean
}