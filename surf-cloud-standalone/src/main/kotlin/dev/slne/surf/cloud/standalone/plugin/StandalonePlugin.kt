package dev.slne.surf.cloud.standalone.plugin

import org.pf4j.PluginWrapper
import org.pf4j.spring.SpringPlugin
import kotlin.io.path.Path

abstract class StandalonePlugin protected constructor(wrapper: PluginWrapper) :
    SpringPlugin(wrapper) {

    val dataFolder = Path("plugins")
}
