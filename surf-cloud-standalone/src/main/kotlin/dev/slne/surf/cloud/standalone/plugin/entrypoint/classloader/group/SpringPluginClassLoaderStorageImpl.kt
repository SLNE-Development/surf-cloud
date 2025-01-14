package dev.slne.surf.cloud.standalone.plugin.entrypoint.classloader.group

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.server.server.plugin.provider.classloader.ClassloaderAccess
import dev.slne.surf.cloud.api.server.server.plugin.provider.classloader.SpringPluginClassloader
import dev.slne.surf.cloud.api.server.server.plugin.provider.classloader.SpringPluginClassloaderGroup
import dev.slne.surf.cloud.api.server.server.plugin.provider.classloader.SpringPluginClassloaderStorage
import java.util.concurrent.CopyOnWriteArrayList

/**
 * This is used for connecting multiple classloaders.
 */
@AutoService(SpringPluginClassloaderStorage::class)
class SpringPluginClassLoaderStorageImpl : SpringPluginClassloaderStorage {
    private val globalGroup = GlobalPluginClassLoaderGroup()
    private val groups = CopyOnWriteArrayList<SpringPluginClassloaderGroup>().apply {
        add(globalGroup)
    }

    override fun registerOpenGroup(classloader: SpringPluginClassloader): SpringPluginClassloaderGroup {
        return registerGroup(classloader, globalGroup)
    }

    override fun registerAccessBackedGroup(
        classloader: SpringPluginClassloader,
        access: ClassloaderAccess
    ): SpringPluginClassloaderGroup {
        val allowedLoaders = globalGroup.classloaders.filter { access.canAccess(it) }.toMutableList()
        return registerGroup(classloader, StaticPluginClassLoaderGroup(allowedLoaders, access, classloader))
    }

    private fun registerGroup(
        classloader: SpringPluginClassloader,
        group: SpringPluginClassloaderGroup
    ): SpringPluginClassloaderGroup {
        var group = group
        // Now add this classloader to any groups that allows it (includes global)
        for (loaderGroup in groups) {
            if (loaderGroup.access.canAccess(classloader)) {
                loaderGroup.add(classloader)
            }
        }

        group = LockingClassLoaderGroup(group)
        groups.add(group)
        return group
    }

    override fun unregisterClassloader(classloader: SpringPluginClassloader) {
        globalGroup.remove(classloader)
        groups.remove(classloader.group)
        for (group in groups) {
            group.remove(classloader)
        }
    }

    override fun registerUnsafePlugin(classloader: SpringPluginClassloader): Boolean {
        if (globalGroup.classloaders.contains(classloader)) {
            return false
        } else {
            globalGroup.add(classloader)
            return true
        }
    }
}