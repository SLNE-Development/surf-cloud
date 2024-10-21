package dev.slne.surf.cloud.standalone.plugin

import dev.slne.surf.cloud.api.util.findAnnotation
import dev.slne.surf.cloud.api.util.getValue
import dev.slne.surf.cloud.api.util.ultimateTargetClass

abstract class StandalonePlugin protected constructor() {

    val meta = ultimateTargetClass().findAnnotation<StandalonePluginMeta>()
        ?: error("Plugin class must be annotated with @StandalonePluginMeta")

    val id: String by meta
    val dataFolder by lazy { StandalonePluginManager.PLUGIN_DIRECTORY.resolve(id) }

    abstract fun start()

    abstract fun stop()
}
