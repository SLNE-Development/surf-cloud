package dev.slne.surf.cloud.standalone.plugin

import org.intellij.lang.annotations.Pattern
import org.springframework.stereotype.Component

private const val ID_PATTERN: String = "[a-z0-9_-]+"

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
annotation class StandalonePluginMeta(@get:PluginIdPattern val id: String)

@Pattern(ID_PATTERN)
annotation class PluginIdPattern
