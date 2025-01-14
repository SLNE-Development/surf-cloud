package dev.slne.surf.cloud.standalone.plugin.storage

import dev.slne.surf.cloud.standalone.plugin.entrypoint.strategy.PluginLoadingStrategy
import dev.slne.surf.cloud.standalone.plugin.entrypoint.strategy.ProviderConfiguration


abstract class ConfiguredProviderStorage<T>(onLoad: ProviderConfiguration<T>) :
    SimpleProviderStorage<T>(PluginLoadingStrategy(onLoad))