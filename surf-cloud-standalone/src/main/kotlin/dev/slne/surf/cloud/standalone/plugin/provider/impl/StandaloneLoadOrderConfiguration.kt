package dev.slne.surf.cloud.standalone.plugin.provider.impl

import dev.slne.surf.cloud.standalone.plugin.provider.configuration.LoadOrderConfiguration
import dev.slne.surf.cloud.standalone.plugin.provider.configuration.StandalonePluginMeta

class StandaloneLoadOrderConfiguration(override val meta: StandalonePluginMeta): LoadOrderConfiguration {
    override val loadBefore = meta.loadBefore
    override val loadAfter = meta.loadAfter
}