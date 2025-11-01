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

fun pluginContext(classloader: SpringPluginClassloader) = classloader.context
val context
    get() = pluginContext(
        getCallerClass()?.classLoader as? SpringPluginClassloader
            ?: error("Not in plugin classloader!")
    )

val currentContext
    get() = pluginContext(
        getCallerClass()?.classLoader as? SpringPluginClassloader
            ?: error("Not in plugin classloader!")
    )

fun <T> bean(clazz: Class<T>, classloader: SpringPluginClassloader): T {
    return classloader.context.getBean(clazz)
}

fun <T> bean(clazz: Class<T>): T {
    return bean(
        clazz, getCallerClass()?.classLoader as? SpringPluginClassloader
            ?: error("Not in plugin classloader!")
    )
}

inline fun <reified T> bean(): T {
    return bean(T::class.java)
}