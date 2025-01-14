package dev.slne.surf.cloud.standalone.plugin.provider.type

import dev.slne.surf.cloud.standalone.plugin.entrypoint.Entrypoint
import dev.slne.surf.cloud.standalone.plugin.entrypoint.EntrypointHandler
import dev.slne.surf.cloud.standalone.plugin.provider.configuration.StandalonePluginMeta
import dev.slne.surf.cloud.standalone.plugin.provider.impl.StandalonePluginParent
import java.nio.file.Path
import java.util.jar.JarFile

object StandalonePluginFileType {
    const val STANDALONE_PLUGIN_YML = "standalone-plugin.yml"

    fun isValidType(file: JarFile): Boolean {
        val entry = file.getJarEntry(STANDALONE_PLUGIN_YML)
        return entry != null
    }

    fun loadMeta(file: JarFile): StandalonePluginMeta {
        val entry = file.getJarEntry(STANDALONE_PLUGIN_YML)
        return StandalonePluginParent.factory.create(file, entry)
    }

    fun register(entrypointHandler: EntrypointHandler, file: JarFile, context: Path) {
        val meta = loadMeta(file)
        val provider = StandalonePluginParent.factory.build(file, meta, context)
        register(entrypointHandler, provider)
    }

    private fun register(entrypointHandler: EntrypointHandler, provider: StandalonePluginParent) {
        var bootstrapProvider: StandalonePluginParent.StandaloneBootstrapProvider? = null
        if (provider.shouldCreateBootstrap()) {
            bootstrapProvider = provider.createBootstrapProvider()
            entrypointHandler.register(Entrypoint.SPRING_PLUGIN_BOOTSTRAPPER, bootstrapProvider)
        }

        entrypointHandler.register(
            Entrypoint.SPRING_PLUGIN,
            provider.createPluginProvider(bootstrapProvider)
        )
    }
}