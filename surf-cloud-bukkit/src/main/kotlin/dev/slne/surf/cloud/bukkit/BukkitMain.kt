package dev.slne.surf.cloud.bukkit

import dev.slne.surf.cloud.api.common.exceptions.FatalSurfError
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.springframework.core.NestedRuntimeException

class BukkitMain : JavaPlugin() {
    override fun onLoad() {
        try {
            bukkitCloudInstance.onLoad()
        } catch (t: Throwable) {
            handleThrowable(t)
        }
    }

    override fun onEnable() {
        try {
            bukkitCloudInstance.onEnable()
        } catch (t: Throwable) {
            handleThrowable(t)
        }

        server.scheduler.runTaskLater(this, Runnable {
            try {
                bukkitCloudInstance.afterStart()
            } catch (t: Throwable) {
                handleThrowable(t)
            }
        }, 1L)
    }

    override fun onDisable() {
        try {
            bukkitCloudInstance.onDisable()
        } catch (t: Throwable) {
            handleThrowable(t)
        }
    }

    val classLoader0: ClassLoader
        get() = classLoader

    private fun handleThrowable(t: Throwable) {
        if (t is FatalSurfError) {
            handleFatalError(t)
        } else if (t is NestedRuntimeException && t.rootCause is FatalSurfError) {
            handleFatalError(t.rootCause as FatalSurfError)
        } else {
            componentLogger.error("An unexpected error occurred", t)
        }
    }

    private fun handleFatalError(fatalError: FatalSurfError) {
        componentLogger.error("A fatal error occurred: ")
        componentLogger.error(fatalError.buildMessage())
        fatalError.printStackTrace()
        Bukkit.shutdown()
    }

    companion object {
        @JvmStatic
        val instance: BukkitMain
            get() = getPlugin(BukkitMain::class.java)
    }
}
