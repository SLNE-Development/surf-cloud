package dev.slne.surf.cloud.api.server.plugin.utils

import dev.slne.surf.cloud.api.server.plugin.provider.classloader.SpringPluginClassloader
import dev.slne.surf.surfapi.core.api.util.getCallerClass
import org.springframework.beans.factory.getBean

fun currentDb(classloader: SpringPluginClassloader) =
    PluginUtilProxies.springTransactionManagerProxy.getCurrentDatabase(classloader.context.getBean())

val currentDb
    get() = currentDb(
        getCallerClass()?.classLoader as? SpringPluginClassloader
            ?: error("Not in plugin classloader!")
    )
