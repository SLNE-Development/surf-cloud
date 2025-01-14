package dev.slne.surf.cloud.api.server.server.plugin

import dev.slne.surf.cloud.api.common.util.requiredService


interface PluginManager {
    companion object {
        val instance = requiredService<PluginManager>()
    }

    fun getPlugin(name: String): StandalonePlugin?
    fun getPlugins(): Array<StandalonePlugin>
    fun isPluginEnabled(name: String): Boolean
    fun isPluginEnabled(plugin: StandalonePlugin?): Boolean

    suspend fun disablePlugins()
    suspend fun clearPlugins()

    suspend fun enablePlugin(plugin: StandalonePlugin)
    suspend fun disablePlugin(plugin: StandalonePlugin)
}