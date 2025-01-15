package dev.slne.surf.cloud.standalone.plugin.loader

import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.api.server.plugin.bootstrap.StandalonePluginProviderContext
import dev.slne.surf.cloud.api.server.plugin.loader.PluginClasspathBuilder
import dev.slne.surf.cloud.api.server.plugin.loader.library.ClassPathLibrary
import dev.slne.surf.cloud.standalone.plugin.entrypoint.classloader.ByteCodeModifyingURLClassloader
import dev.slne.surf.cloud.standalone.plugin.entrypoint.classloader.SpringPluginClassloaderImpl
import dev.slne.surf.cloud.standalone.plugin.loader.library.StandaloneLibraryStore
import dev.slne.surf.cloud.standalone.plugin.provider.configuration.StandalonePluginMeta
import it.unimi.dsi.fastutil.objects.ObjectList
import java.io.IOException
import java.net.URL
import java.nio.file.Path
import java.util.jar.JarFile

class StandaloneClasspathBuilder(
    override val context: StandalonePluginProviderContext
) : PluginClasspathBuilder {

    private val libraries = mutableObjectListOf<ClassPathLibrary>()

    override fun addLibrary(classPathLibrary: ClassPathLibrary) = apply {
        libraries.add(classPathLibrary)
    }

    fun buildClassLoader(
        source: Path,
        jarFile: JarFile,
        meta: StandalonePluginMeta
    ): SpringPluginClassloaderImpl {
        val paths = buildLibraryPaths()
        val urls = Array<URL>(paths.size) { paths[it].toUri().toURL() }

        try {
            val libraryLoader = ByteCodeModifyingURLClassloader(urls, javaClass.classLoader)
            return SpringPluginClassloaderImpl(
                source,
                jarFile,
                meta,
                javaClass.classLoader,
                libraryLoader
            )
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun buildLibraryPaths(): ObjectList<Path> {
        val store = StandaloneLibraryStore()
        libraries.forEach { it.register(store) }
        return store.paths
    }
}