@file:OptIn(InternalPluginApi::class)

package dev.slne.surf.cloud.standalone.plugin.entrypoint.classloader

import dev.slne.surf.cloud.api.server.server.plugin.InternalPluginApi
import dev.slne.surf.cloud.api.server.server.plugin.StandalonePlugin
import dev.slne.surf.cloud.api.server.server.plugin.provider.classloader.SpringPluginClassloader
import dev.slne.surf.cloud.api.server.server.plugin.provider.classloader.SpringPluginClassloaderGroup
import dev.slne.surf.cloud.api.server.server.plugin.provider.classloader.SpringPluginClassloaderStorage
import dev.slne.surf.cloud.api.server.server.plugin.configuration.PluginMeta
import dev.slne.surf.cloud.api.server.server.plugin.dependency.DependencyContext
import dev.slne.surf.cloud.standalone.plugin.PluginInitializerManager
import dev.slne.surf.cloud.standalone.plugin.manager.SpringPluginManagerImpl
import dev.slne.surf.surfapi.core.api.util.toEnumeration
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import java.util.*
import java.util.jar.JarFile
import kotlin.io.path.div

class SpringPluginClassloaderImpl(
    source: Path,
    jar: JarFile,
    meta: PluginMeta,
    parent: ClassLoader,
    val libraryLoader: URLClassLoader? = null
) : SimpleSpringPluginClassloader(source, jar, meta, parent), SpringPluginClassloader {
    companion object {
        init {
            registerAsParallelCapable()
        }
    }

    override var plugin: StandalonePlugin? = null
    override var group: SpringPluginClassloaderGroup? = null

    fun refreshClassloaderDependencyTree(context: DependencyContext) {
        val group = group
        if (group != null) {
            SpringPluginClassloaderStorage.instance.unregisterClassloader(this)
        }

        this.group = SpringPluginClassloaderStorage.instance.registerAccessBackedGroup(this) {
            context.isTransitiveDependency(meta, it.meta)
        }
    }

    override fun getResource(name: String) = findResource(name) ?: libraryLoader?.getResource(name)

    override fun getResources(name: String): Enumeration<URL> = findResources(name).asSequence()
        .plus(libraryLoader?.getResources(name)?.asSequence() ?: emptySequence())
        .toEnumeration()

    override fun loadClass(name: String, resolve: Boolean): Class<*>? {
        return loadClass(name, resolve, true, true)
    }

    override fun loadClass(
        name: String,
        resolve: Boolean,
        checkGlobal: Boolean,
        checkLibraries: Boolean
    ): Class<*> {
        try {
            val clazz = super.loadClass(name, resolve)

            if (checkGlobal || clazz.classLoader == this) {
                return clazz
            }
        } catch (_: ClassNotFoundException) {
        }

        if (checkLibraries && libraryLoader != null) {
            try {
                return libraryLoader.loadClass(name)
            } catch (_: ClassNotFoundException) {
            }
        }

        if (checkGlobal) {
            val group = group ?: error("Tried to resolve class while group was not initialized yet")
            val clazz = group.classByName(name, resolve, this)

            if (clazz != null) {
                return clazz
            }
        }

        throw ClassNotFoundException(name)
    }

    override fun init(plugin: StandalonePlugin) {
        plugin.init(meta, PluginInitializerManager.pluginDirectoryPath / meta.name, this)
        this.plugin = plugin
    }

    override fun close() {
        super.close()
        jar.close()
        libraryLoader?.close()
    }
}