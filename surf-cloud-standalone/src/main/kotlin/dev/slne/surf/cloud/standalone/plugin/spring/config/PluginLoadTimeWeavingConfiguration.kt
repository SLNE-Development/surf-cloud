package dev.slne.surf.cloud.standalone.plugin.spring.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration(proxyBeanMethods = false)
@Profile("plugin")
class PluginLoadTimeWeavingConfiguration