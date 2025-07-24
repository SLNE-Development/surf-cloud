package dev.slne.surf.cloudtest.standalone.test

import dev.slne.surf.cloud.api.server.plugin.loader.PluginClasspathBuilder
import dev.slne.surf.cloud.api.server.plugin.loader.StandalonePluginLoader
import dev.slne.surf.cloud.api.server.plugin.loader.library.impl.MavenLibraryResolver

class TestStandaloneLoader : StandalonePluginLoader {
    override fun classloader(classpathBuilder: PluginClasspathBuilder) {
        repeat(20) {
            println("# Loading $it")
        }

        val resolver = MavenLibraryResolver()
        resolver.addMavenCentral()

        resolver.addDependency("commons-io:commons-io:2.18.0")

        classpathBuilder.addLibrary(resolver)
    }
}