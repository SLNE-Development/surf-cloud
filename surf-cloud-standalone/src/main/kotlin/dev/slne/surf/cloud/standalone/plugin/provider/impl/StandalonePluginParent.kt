package dev.slne.surf.cloud.standalone.plugin.provider.impl

import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.api.server.server.plugin.StandalonePlugin
import dev.slne.surf.cloud.api.server.server.plugin.bootstrap.StandalonePluginBootstrap
import dev.slne.surf.cloud.api.server.server.plugin.bootstrap.StandalonePluginProviderContext
import dev.slne.surf.cloud.api.server.server.plugin.dependency.DependencyContext
import dev.slne.surf.cloud.api.server.server.plugin.provider.ProviderLoader
import dev.slne.surf.cloud.standalone.plugin.entrypoint.classloader.SpringPluginClassloaderImpl
import dev.slne.surf.cloud.standalone.plugin.entrypoint.dependency.DependencyContextHolder
import dev.slne.surf.cloud.standalone.plugin.provider.PluginProvider
import dev.slne.surf.cloud.standalone.plugin.provider.ProviderStatus
import dev.slne.surf.cloud.standalone.plugin.provider.ProviderStatusHolder
import dev.slne.surf.cloud.standalone.plugin.provider.configuration.LoadOrderConfiguration
import dev.slne.surf.cloud.standalone.plugin.provider.configuration.StandalonePluginMeta
import dev.slne.surf.cloud.standalone.plugin.provider.impl.StandalonePluginProviderFactory
import java.nio.file.Path
import java.util.jar.JarFile

class StandalonePluginParent(
    private val path: Path,
    private val jarFile: JarFile,
    private val meta: StandalonePluginMeta,
    private val classloader: SpringPluginClassloaderImpl,
    private val context: StandalonePluginProviderContext
) {

    private val log = logger()

    companion object {
        val factory = StandalonePluginProviderFactory()
    }

    fun shouldCreateBootstrap() = meta.bootstrapper != null
    fun createBootstrapProvider() = StandaloneBootstrapProvider()
    fun createPluginProvider(bootstrapProvider: StandaloneBootstrapProvider?) =
        StandalonePluginProvider(bootstrapProvider)

    inner class StandaloneBootstrapProvider : PluginProvider<StandalonePluginBootstrap>,
        ProviderStatusHolder, DependencyContextHolder {
        override var status: ProviderStatus = ProviderStatus.UNINITIALIZED
        override val source = path
        override val file = jarFile
        override val meta = this@StandalonePluginParent.meta
        var lastProvided: StandalonePluginBootstrap? = null
            private set

        override fun createInstance(): StandalonePluginBootstrap {
            val bootstrap = ProviderLoader.loadClass(
                meta.bootstrapper!!,
                StandalonePluginBootstrap::class.java,
                classloader
            ) { status = ProviderStatus.ERRORED }
            status = ProviderStatus.INITIALIZED
            lastProvided = bootstrap
            return bootstrap
        }

        override fun createConfiguration(toLoad: Map<String, PluginProvider<*>>): LoadOrderConfiguration {
            return StandaloneBootstrapLoadOrderConfiguration(meta)
        }

        override fun validateDependencies(context: DependencyContext): Set<String> {
            return meta.bootstrapDependencies
                .filter { (dep, conf) -> conf.required && !context.hasDependency(dep) }
                .keys
        }

        override fun setContext(context: DependencyContext) {
            classloader.refreshClassloaderDependencyTree(context)
        }

        override fun toString(): String {
            return "StandaloneBootstrapProvider(parent=${this@StandalonePluginParent}, status=$status, lastProvided=$lastProvided)"
        }

    }

    inner class StandalonePluginProvider(private val bootstrapProvider: StandaloneBootstrapProvider?) :
        PluginProvider<StandalonePlugin>, ProviderStatusHolder, DependencyContextHolder {
        override var status: ProviderStatus = ProviderStatus.UNINITIALIZED
        override val source = path
        override val file = jarFile
        override val meta = this@StandalonePluginParent.meta
        override fun createInstance(): StandalonePlugin {
            val bootstrap = this.bootstrapProvider?.lastProvided

            return try {
                val plugin = bootstrap?.createPlugin(context) ?: ProviderLoader.loadClass(
                    meta.main,
                    StandalonePlugin::class.java,
                    classloader
                )

                val cls = Class.forName(meta.main, true, plugin::class.java.classLoader)
                if (!plugin::class.java.isAssignableFrom(cls)) {
                    log.atInfo()
                        .log("Bootstrap of plugin ${meta.name} provided a plugin instance of class ${plugin::class.java.name} which does not match the plugin declared main class")
                }

                status = ProviderStatus.INITIALIZED
                plugin
            } catch (e: Exception) {
                status = ProviderStatus.ERRORED
                throw e
            }
        }

        override fun createConfiguration(toLoad: Map<String, PluginProvider<*>>): LoadOrderConfiguration {
            return StandaloneLoadOrderConfiguration(meta)
        }

        override fun validateDependencies(context: DependencyContext): Set<String> {
            return meta.serverDependencies
                .filter { (dep, conf) -> conf.required && !context.hasDependency(dep) }
                .keys
        }

        fun shouldSkipCreation() = bootstrapProvider?.status == ProviderStatus.ERRORED

        override fun setContext(context: DependencyContext) {
            classloader.refreshClassloaderDependencyTree(context)
        }

        override fun toString(): String {
            return "StandalonePluginProvider(parent=${this@StandalonePluginParent}, bootstrapProvider=$bootstrapProvider, status=$status)"
        }

    }

    override fun toString(): String {
        return "StandalonePluginParent(path=$path, jarFile=$jarFile, meta=$meta, classloader=$classloader)"
    }
}
