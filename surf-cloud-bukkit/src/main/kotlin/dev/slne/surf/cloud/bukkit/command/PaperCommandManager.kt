package dev.slne.surf.cloud.bukkit.command

import dev.slne.surf.cloud.bukkit.command.broadcast.broadcastCommand
import dev.slne.surf.cloud.bukkit.command.connection.disconnectPlayerCommand
import dev.slne.surf.cloud.bukkit.command.connection.timeoutPlayerCommand
import dev.slne.surf.cloud.bukkit.command.lastseen.lastSeenCommand
import dev.slne.surf.cloud.bukkit.command.network.findCommand
import dev.slne.surf.cloud.bukkit.command.network.glistCommand
import dev.slne.surf.cloud.bukkit.command.network.sendCommand
import dev.slne.surf.cloud.bukkit.command.network.serverCommand
import dev.slne.surf.cloud.bukkit.command.playtime.playtimeCommand

object PaperCommandManager {
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