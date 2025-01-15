package dev.slne.surf.cloud.standalone.plugin.provider.configuration

import dev.slne.surf.cloud.api.server.plugin.configuration.PluginMeta

interface LoadOrderConfiguration {
    val loadBefore: Set<String>
    val loadAfter: Set<String>
    val meta: PluginMeta
}