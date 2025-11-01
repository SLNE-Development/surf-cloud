package dev.slne.surf.cloud.standalone.spark.provider

import dev.slne.surf.cloud.api.server.plugin.provider.classloader.SpringPluginClassloader
import me.lucko.spark.common.sampler.source.ClassSourceLookup

object CloudClassSourceLookup : ClassSourceLookup.ByClassLoader() {
    override fun identify(classloader: ClassLoader): String? {
        if (classloader is SpringPluginClassloader) {
            return classloader.meta.name
        }

        return null
    }
}