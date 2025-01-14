package dev.slne.surf.cloud.standalone.plugin.provider.source

import dev.slne.surf.cloud.standalone.plugin.entrypoint.EntrypointHandler
import dev.slne.surf.cloud.standalone.plugin.provider.type.StandalonePluginFileType
import java.nio.file.Path
import java.util.jar.JarFile
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.notExists

class FileProviderSource(
    private val contextChecker: (Path) -> String
) : ProviderSource<Path, Path> {
    override fun prepareContext(context: Path): Path {
        val source = contextChecker(context)

        require(!context.notExists()) { "$source does not exist, cannot load a plugin from it!" }
        require(context.isRegularFile()) { "$source is not a file, cannot load a plugin from it!" }
        require(context.extension == "jar") { "$source is not a jar file, cannot load a plugin from it!" }

        return context
    }

    override fun registerProviders(
        entrypointHandler: EntrypointHandler,
        context: Path
    ) {
        val source = contextChecker(context)
        val file = JarFile(context.toFile(), true, JarFile.OPEN_READ, JarFile.runtimeVersion())

        require(StandalonePluginFileType.isValidType(file)) { "$source does not contain a ${StandalonePluginFileType.STANDALONE_PLUGIN_YML}! Cannot load a plugin from it!" }
        StandalonePluginFileType.register(entrypointHandler, file, context)
    }
}