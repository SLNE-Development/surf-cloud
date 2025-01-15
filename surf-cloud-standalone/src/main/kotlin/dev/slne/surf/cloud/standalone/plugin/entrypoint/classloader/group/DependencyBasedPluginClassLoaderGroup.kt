package dev.slne.surf.cloud.standalone.plugin.entrypoint.classloader.group

import dev.slne.surf.cloud.api.server.plugin.provider.classloader.ClassloaderAccess

class DependencyBasedPluginClassLoaderGroup(
    private val globalPluginClassLoaderGroup: GlobalPluginClassLoaderGroup,
    override val access: ClassloaderAccess
) : SimpleListPluginClassLoaderGroup(ArrayList()) {

    /**
     * This will refresh the dependencies of the current classloader.
     */
    fun populateDependencies() {
        classloaders.clear()
        for (classloader in globalPluginClassLoaderGroup.classloaders) {
            if (access.canAccess(classloader)) {
                classloaders.add(classloader)
            }
        }
    }

    override fun toString(): String {
        return "DependencyBasedPluginClassLoaderGroup(access=$access, globalPluginClassLoaderGroup=$globalPluginClassLoaderGroup) ${super.toString()}"
    }
}