package dev.slne.surf.cloud.standalone.plugin.provider.type

import dev.slne.surf.cloud.api.server.plugin.configuration.PluginMeta
import java.nio.file.Path
import java.util.jar.JarEntry
import java.util.jar.JarFile

interface PluginTypeFactory<T, C: PluginMeta> {
    fun build(file: JarFile, meta: C, source: Path): T
    fun create(file: JarFile, config: JarEntry): C
}