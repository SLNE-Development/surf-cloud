package dev.slne.surf.cloud.standalone.plugin.entrypoint.classloader

import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import dev.slne.surf.surfapi.core.api.util.synchronize
import java.io.IOException
import java.io.UncheckedIOException
import java.net.JarURLConnection
import java.net.URI
import java.net.URL
import java.net.URLClassLoader
import java.security.CodeSigner
import java.security.CodeSource
import java.util.jar.Attributes
import java.util.jar.Manifest

class ByteCodeModifyingURLClassloader(
    urls: Array<URL>,
    parent: ClassLoader,
    private val modifier: (ByteArray) -> ByteArray = { it }
) : URLClassLoader(urls, parent) {
    companion object {
        private val missingManifest = Any()

        init {
            registerAsParallelCapable()
        }
    }

    private val manifestCache = mutableObject2ObjectMapOf<String, Any>().synchronize()

    override fun findClass(name: String): Class<*> {
        val path = name.replace('.', '/') + ".class"
        val url = findResource(path) ?: throw ClassNotFoundException(name)

        return try {
            defineClass(name, url)
        } catch (_: IOException) {
            throw ClassNotFoundException(name)
        }
    }

    private fun defineClass(name: String, url: URL): Class<*> {
        val lastDot = name.lastIndexOf('.')

        if (lastDot != -1) {
            val pkgName = name.substring(0, lastDot)

            // Check if package already loaded.
            val manifest = url.manifest()
            val jarUrl = URI.create(url.jarName()).toURL()

            if (getAndVerifyPackage(pkgName, manifest, jarUrl) == null) {
                try {
                    if (manifest != null) {
                        definePackage(pkgName, manifest, jarUrl)
                    } else {
                        definePackage(pkgName, null, null, null, null, null, null, null)
                    }
                } catch (e: IllegalArgumentException) {
                    if (getAndVerifyPackage(pkgName, manifest, jarUrl) == null) {
                        throw AssertionError("Package $pkgName already defined", e)
                    }
                }
            }
        }

        val bytes = url.openStream().use { it.readAllBytes() }
        val modifiedBytes = modifier(bytes)

        val codeSource = CodeSource(url, null as Array<CodeSigner>?)
        return defineClass(name, modifiedBytes, 0, modifiedBytes.size, codeSource)
    }

    private fun getAndVerifyPackage(pkgName: String, manifest: Manifest?, url: URL): Package? {
        val pkg = getDefinedPackage(pkgName) ?: return null

        if (pkg.isSealed) {
            if (!pkg.isSealed(url)) {
                throw SecurityException("Package $pkgName is sealed")
            }
        } else {
            if (manifest != null && manifest.isSealed(pkgName)) {
                throw SecurityException("Cannot seal package $pkgName")
            }
        }

        return pkg
    }

    private fun Manifest.isSealed(name: String) = // We love one-liner
        (getAttributes(name.replace('.', '/').plus('/'))?.getValue(Attributes.Name.SEALED)
            ?: mainAttributes.getValue(Attributes.Name.SEALED)).equals("true", ignoreCase = true)


    private fun URL.manifest(): Manifest? {
        if (protocol != "jar") return null

        return try {
            manifestCache.computeIfAbsent(jarName()) {
                (openConnection() as JarURLConnection).manifest ?: missingManifest
            } as? Manifest
        } catch (e: UncheckedIOException) {
            throw e.cause!!
        }
    }
}

private fun URL.jarName() =
    path.substringBeforeLast('!').also { require(it != path) { "Not a jar URL: $this" } }
