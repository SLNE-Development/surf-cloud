package dev.slne.surf.cloud.bukkit.command

import dev.slne.surf.cloud.api.common.util.TimeLogger
import dev.slne.surf.cloud.bukkit.command.broadcast.broadcastCommand
import dev.slne.surf.cloud.bukkit.command.connection.disconnectPlayerCommand
import dev.slne.surf.cloud.bukkit.command.connection.timeoutPlayerCommand
import dev.slne.surf.cloud.bukkit.command.lastseen.lastSeenCommand
import dev.slne.surf.cloud.bukkit.command.network.findCommand
import dev.slne.surf.cloud.bukkit.command.network.glistCommand
import dev.slne.surf.cloud.bukkit.command.network.sendCommand
import dev.slne.surf.cloud.bukkit.command.network.serverCommand
import dev.slne.surf.cloud.bukkit.command.playtime.playtimeCommand
import dev.slne.surf.cloud.core.common.spring.CloudLifecycleAware
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(CloudLifecycleAware.MISC_PRIORITY)
class PaperCommandManager : CloudLifecycleAware {

    override suspend fun onEnable(timeLogger: TimeLogger) {
        timeLogger.measureStep("Register Cloud commands") {
            registerCommands()
        }
    }

    fun registerCommands() {
        findCommand()
        serverCommand()
        sendCommand()
        playtimeCommand()
        lastSeenCommand()
        broadcastCommand()
        glistCommand()
        timeoutPlayerCommand()
        disconnectPlayerCommand()
    }
}