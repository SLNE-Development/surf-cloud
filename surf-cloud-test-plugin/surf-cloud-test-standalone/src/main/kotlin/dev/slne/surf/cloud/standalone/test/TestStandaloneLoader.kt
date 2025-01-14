package dev.slne.surf.cloud.standalone.test

import dev.slne.surf.cloud.api.server.server.plugin.loader.PluginClasspathBuilder
import dev.slne.surf.cloud.api.server.server.plugin.loader.StandalonePluginLoader
import dev.slne.surf.cloud.api.server.server.plugin.loader.library.impl.MavenLibraryResolver

class TestStandaloneLoader : StandalonePluginLoader {
    override fun classloader(classpathBuilder: PluginClasspathBuilder) {
        repeat(20) {
            println("# Loading $it")
        }

        val resolver = MavenLibraryResolver()
        resolver.addMavenCentral()

//        resolver.addDependency(Dependency(DefaultArtifact("space.kscience:kmath-core:0.4.1"), "compile"))
        resolver.addDependency("commons-io:commons-io:2.18.0")

        classpathBuilder.addLibrary(resolver)
    }
}