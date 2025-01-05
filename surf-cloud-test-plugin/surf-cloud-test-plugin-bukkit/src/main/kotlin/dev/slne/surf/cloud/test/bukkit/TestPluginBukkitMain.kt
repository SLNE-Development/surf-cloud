package dev.slne.surf.cloud.test.bukkit

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.cloud.test.core.testModelService
import kotlinx.coroutines.delay
import org.bukkit.plugin.java.JavaPlugin
import kotlin.time.Duration.Companion.seconds

val plugin get() = JavaPlugin.getPlugin(TestPluginBukkitMain::class.java)

class TestPluginBukkitMain : SuspendingJavaPlugin() {
    override suspend fun onEnableAsync() {
        plugin.launch {
            while (true) {
                val models = testModelService.findAll()

                logger.info("#".repeat(50))
                logger.info("Found ${models.size} models.")
                models.forEach {
                    logger.info(it.toString())
                }
                logger.info("#".repeat(50))

                delay(5.seconds)
            }
        }
    }
}