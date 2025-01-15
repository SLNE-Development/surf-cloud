package dev.slne.surf.cloud.api.server.plugin.provider

import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
object ProviderLoader {
    fun <T> loadClass(
        clazz: String,
        type: Class<T>,
        loader: ClassLoader,
        onError: (() -> Unit)? = null
    ): T = try {
        try {
            val jarClass = Class.forName(clazz, true, loader)
            val pluginClass = try {
                jarClass.asSubclass(type)
            } catch (_: ClassCastException) {
                throw ClassCastException("Class '$clazz' does not extend '$type'")
            }

            pluginClass.kotlin.objectInstance ?: pluginClass.getDeclaredConstructor().newInstance()
        } catch (_: IllegalAccessException) {
            error("No public Constructor")
        } catch (e: InstantiationError) {
            throw RuntimeException("Abnormal class instantiation", e)
        }
    } catch (e: Throwable) {
        onError?.invoke()
        throw e
    }
}