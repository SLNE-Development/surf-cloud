package dev.slne.surf.cloud.api.common.util.classloader

import java.io.IOException
import java.net.URL
import java.nio.ByteBuffer
import java.util.*
import kotlin.math.max
import kotlin.math.min

class JoinClassLoader(
    parent: ClassLoader?,
    private var delegateClassLoaders: Array<out ClassLoader>
) : ClassLoader(parent) {

    constructor(parent: ClassLoader?, delegateClassLoaders: Collection<ClassLoader>) : this(
        parent,
        delegateClassLoaders.toTypedArray()
    )

    @Throws(ClassNotFoundException::class)
    override fun findClass(name: String): Class<*> {
        val parentClass = parent?.loadClass(name)

        if (parentClass != null) {
            return parentClass
        }

        val path = name.replace('.', '/') + ".class"
        val url = findResource(path)
        val byteCode: ByteBuffer

        if (url == null) {
            throw ClassNotFoundException(name)
        }

        try {
            byteCode = loadResource(url)
        } catch (exception: IOException) {
            throw ClassNotFoundException(name, exception)
        }

        return defineClass(name, byteCode, null)
    }

    override fun findResource(name: String): URL? = parent?.getResource(name)
        ?: delegateClassLoaders.firstNotNullOfOrNull { it.getResource(name) }


    @Throws(IOException::class)
    override fun findResources(name: String): Enumeration<URL> {
        val vector = Vector<URL>()

        parent?.getResources(name)?.toList()?.let { vector.addAll(it) }
        vector += delegateClassLoaders.flatMap { it.getResources(name).asSequence() }

        return vector.elements()
    }

    /**
     * Load resource byte buffer.
     *
     * @param url the url
     * @return the byte buffer
     * @throws IOException the io exception
     */
    @Throws(IOException::class)
    private fun loadResource(url: URL): ByteBuffer {
        url.openStream().use { stream ->
            var initialBufferCapacity = min(0x40000, stream.available() + 1)
            initialBufferCapacity =
                if (initialBufferCapacity <= 2) 0x10000 else max(initialBufferCapacity, 0x200)


            var buffer = ByteBuffer.allocate(initialBufferCapacity)

            while (true) {
                if (!buffer.hasRemaining()) {
                    val newBuf = ByteBuffer.allocate(buffer.capacity() * 2)

                    buffer.flip()
                    newBuf.put(buffer)
                    buffer = newBuf
                }

                val length = stream.read(buffer.array(), buffer.position(), buffer.remaining())

                if (length <= 0) {
                    break
                }

                buffer.position(buffer.position() + length)
            }

            buffer.flip()
            return buffer
        }
    }

    fun addDelegateClassLoader(classLoader: ClassLoader) {
        val newDelegateClassLoaders = arrayOfNulls<ClassLoader>(delegateClassLoaders.size + 1)
        System.arraycopy(
            delegateClassLoaders, 0, newDelegateClassLoaders, 0,
            delegateClassLoaders.size
        )
        newDelegateClassLoaders[delegateClassLoaders.size] = classLoader
        delegateClassLoaders = newDelegateClassLoaders.requireNoNulls()
    }
}