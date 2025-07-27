package dev.slne.surf.cloud.standalone.plugin.entrypoint.classloader.group

import dev.slne.surf.cloud.api.server.plugin.provider.classloader.SpringPluginClassloader
import dev.slne.surf.cloud.api.server.plugin.provider.classloader.SpringPluginClassloaderGroup
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantReadWriteLock

class LockingClassLoaderGroup(
    private val parent: SpringPluginClassloaderGroup
) : SpringPluginClassloaderGroup {
    private val classLoadLock = mutableObject2ObjectMapOf<String, ClassLockEntry>()
    override val access = parent.access

    override fun classByName(
        name: String,
        resolve: Boolean,
        requester: SpringPluginClassloader
    ): Class<*>? {
        // make MT safe
        val lock = synchronized(classLoadLock) {
            classLoadLock.computeIfAbsent(name) { ClassLockEntry() }
                .apply { count.incrementAndGet() }
        }
        lock.lock.writeLock().lock()

        try {
            return parent.classByName(name, resolve, requester)
        } finally {
            synchronized(classLoadLock) {
                lock.lock.writeLock().unlock()
                if (lock.count.get() == 1) {
                    classLoadLock.remove(name)
                } else {
                    lock.count.decrementAndGet()
                }
            }
        }
    }

    override fun remove(plugin: SpringPluginClassloader) {
        parent.remove(plugin)
    }

    override fun add(plugin: SpringPluginClassloader) {
        parent.add(plugin)
    }

    data class ClassLockEntry(
        val count: AtomicInteger = AtomicInteger(0),
        val lock: ReentrantReadWriteLock = ReentrantReadWriteLock()
    )

    override fun toString(): String {
        return "LockingClassLoaderGroup(parent=$parent, classLoadLock=$classLoadLock)"
    }
}