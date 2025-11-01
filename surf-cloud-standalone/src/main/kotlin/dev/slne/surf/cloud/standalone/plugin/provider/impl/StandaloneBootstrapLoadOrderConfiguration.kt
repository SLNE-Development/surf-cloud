package dev.slne.surf.cloud.standalone.plugin.provider.impl

import dev.slne.surf.cloud.standalone.plugin.provider.configuration.LoadOrderConfiguration
import dev.slne.surf.cloud.standalone.plugin.provider.configuration.StandalonePluginMeta
import dev.slne.surf.cloud.standalone.plugin.provider.configuration.type.DependencyConfiguration

class StandaloneBootstrapLoadOrderConfiguration(override val meta: StandalonePluginMeta) :
    LoadOrderConfiguration {

    // This plugin will load BEFORE all dependencies (so dependencies will load AFTER plugin)
    override val loadBefore = meta.bootstrapDependencies
        .filterValues { it.load == DependencyConfiguration.LoadOrder.AFTER }.keys


    // This plugin will load AFTER all dependencies (so dependencies will load BEFORE plugin)
    override val loadAfter = meta.bootstrapDependencies
        .filterValues { it.load == DependencyConfiguration.LoadOrder.BEFORE }.keys
}