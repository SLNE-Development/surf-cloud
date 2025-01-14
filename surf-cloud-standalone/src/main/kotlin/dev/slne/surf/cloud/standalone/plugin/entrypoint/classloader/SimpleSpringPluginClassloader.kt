package dev.slne.surf.cloud.standalone.plugin.entrypoint.classloader

import dev.slne.surf.cloud.api.common.util.getOrMapAndThrow
import dev.slne.surf.cloud.api.server.server.plugin.configuration.PluginMeta
import java.net.URL
import java.net.URLClassLoader
import java.nio.file.Path
import java.security.CodeSource
import java.util.*
import java.util.jar.JarFile
import java.util.jar.Manifest

open class SimpleSpringPluginClassloader(
    protected val source: Path,
    protected val jar: JarFile,
    val meta: PluginMeta,
    parent: ClassLoader
) : URLClassLoader(source.fileName.toString(), arrayOf(source.toUri().toURL()), parent) {
    companion object {
        init {
            registerAsParallelCapable()
        }
    }

    protected val jarManifest: Manifest? = jar.manifest
    protected val jarUrl: URL = source.toUri().toURL()

    override fun getResource(name: String): URL? {
        return this.findResource(name)
    }

    override fun getResources(name: String): Enumeration<URL> {
        return this.findResources(name)
    }

    override fun findClass(name: String): Class<*> {
        val path = name.replace('.', '/').plus(".class")
        val entry = jar.getJarEntry(path) ?: throw ClassNotFoundException(name)

        val rawBytes = runCatching {
            jar.getInputStream(entry).use { it.readAllBytes() }
        }.getOrMapAndThrow { ClassNotFoundException(name, it) }

        val bytes = ClassloaderBytecodeModifier.instance.modify(meta, rawBytes)
        val dot = name.lastIndexOf('.')

        if (dot != -1) {
            val pkg = name.substring(0, dot)

            if (getDefinedPackage(pkg) == null) {
                try {
                    if (jarManifest != null) {
                        definePackage(pkg, jarManifest, jarUrl)
                    } else {
                        definePackage(pkg, null, null, null, null, null, null, null)
                    }

                } catch (_: IllegalArgumentException) {
                    if (getDefinedPackage(pkg) == null) {
                        error("Package $pkg already defined by another classloader")
                    }
                }
            }
        }

        val signers = entry.codeSigners
        val source = CodeSource(jarUrl, signers)

        return defineClass(name, bytes, 0, bytes.size, source)
    }

    override fun toString(): String {
        return "SimpleSpringPluginClassloader(jar=$jar, source=$source, meta=$meta, jarManifest=$jarManifest, jarUrl=$jarUrl)"
    }
}