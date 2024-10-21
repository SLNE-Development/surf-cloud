package dev.slne.surf.cloud.bukkit

import com.google.gson.Gson
import io.papermc.paper.plugin.loader.PluginClasspathBuilder
import io.papermc.paper.plugin.loader.PluginLoader
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.RemoteRepository
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

@Suppress("UnstableApiUsage")
class BukkitLoader : PluginLoader {
    override fun classloader(classpathBuilder: PluginClasspathBuilder) {
        val resolver = MavenLibraryResolver()
        val pluginLibraries = load()

        for (dependency in pluginLibraries.asDependencies()) {
            resolver.addDependency(dependency)
        }
        for (repository in pluginLibraries.asRepositories()) {
            resolver.addRepository(repository)
        }

        classpathBuilder.addLibrary(resolver)
    }

    private fun load(): PluginLibraries {
        try {
            javaClass.getResourceAsStream("/paper-libraries.json").use {
                return if (it != null) {
                    Gson().fromJson(
                        InputStreamReader(it, StandardCharsets.UTF_8),
                        PluginLibraries::class.java
                    )
                } else {
                    PluginLibraries(emptyMap(), listOf())
                }
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private data class PluginLibraries(
        private val repositories: Map<String, String>?,
        private val dependencies: List<String>?
    ) {
        fun asDependencies() =
            dependencies?.map { Dependency(DefaultArtifact(it), null) } ?: emptyList()


        fun asRepositories() =
            repositories?.map { (id, url) -> RemoteRepository.Builder(id, "default", url).build() }
                ?: emptyList()

    }
}
