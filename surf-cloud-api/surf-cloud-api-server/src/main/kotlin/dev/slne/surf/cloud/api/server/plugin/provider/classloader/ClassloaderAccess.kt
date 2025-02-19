package dev.slne.surf.cloud.api.server.plugin.provider.classloader

import dev.slne.surf.cloud.api.common.util.annotation.InternalApi

@InternalApi
fun interface ClassloaderAccess {

    fun canAccess(plugin: SpringPluginClassloader): Boolean
}