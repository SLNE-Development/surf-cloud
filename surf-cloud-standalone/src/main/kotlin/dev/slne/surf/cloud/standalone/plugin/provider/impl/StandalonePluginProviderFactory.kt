package dev.slne.surf.cloud.standalone.plugin.provider.impl

import dev.slne.surf.cloud.api.server.plugin.loader.StandalonePluginLoader
import dev.slne.surf.cloud.api.server.plugin.provider.ProviderLoader
import dev.slne.surf.cloud.standalone.plugin.bootstrap.PluginProviderContextImpl
import dev.slne.surf.cloud.standalone.plugin.entrypoint.classloader.SimpleSpringPluginClassloader
import dev.slne.surf.cloud.standalone.plugin.loader.StandaloneClasspathBuilder
import dev.slne.surf.cloud.standalone.plugin.provider.configuration.StandalonePluginMeta
import dev.slne.surf.cloud.standalone.plugin.provider.type.PluginTypeFactory
import java.nio.file.Path
import java.util.jar.JarEntry
import java.util.jar.JarFile

class StandalonePluginProviderFactory :
    PluginTypeFactory<StandalonePluginParent, StandalonePluginMeta> {
    override fun build(
        file: JarFile,
        meta: StandalonePluginMeta,
        source: Path
    ): StandalonePluginParent {
        val context = PluginProviderContextImpl.create(meta, source)
        val builder = StandaloneClasspathBuilder(context)

        val loader = meta.loader
        if (loader != null) {
            SimpleSpringPluginClassloader(source, file, meta, javaClass.classLoader).use {
                ProviderLoader.loadClass(loader, StandalonePluginLoader::class.java, it)
                    .classloader(builder)
            }
        }

        val classLoader = builder.buildClassLoader(source, file, meta)
        return StandalonePluginParent(source, file, meta, classLoader, context)
    }

    override fun create(
        file: JarFile,
        config: JarEntry
    ) = file.getInputStream(config).reader().buffered().use {
        StandalonePluginMeta.Companion.create(it)
    }
}