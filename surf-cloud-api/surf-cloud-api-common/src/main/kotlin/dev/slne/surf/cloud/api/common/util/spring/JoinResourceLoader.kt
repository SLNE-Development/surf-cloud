package dev.slne.surf.cloud.api.common.util.spring

import dev.slne.surf.surfapi.core.api.util.emptyObjectList
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader

open class JoinResourceLoader(
    private val mainLoader: ResourceLoader,
    private val joinLoader: List<ResourceLoader> = emptyObjectList()
) : ResourceLoader {
    override fun getResource(location: String): Resource {
        val resource = mainLoader.getResource(location)
        if (resource.exists()) return resource

        for (loader in joinLoader) {
            val joinedResource = loader.getResource(location)
            if (joinedResource.exists()) return joinedResource
        }

        return resource
    }

    override fun getClassLoader(): ClassLoader? {
        val mainClassLoader = mainLoader.classLoader
        if (mainClassLoader != null) return mainClassLoader
        for (loader in joinLoader) {
            val joinedClassLoader = loader.classLoader
            if (joinedClassLoader != null) return joinedClassLoader
        }
        return null
    }
}