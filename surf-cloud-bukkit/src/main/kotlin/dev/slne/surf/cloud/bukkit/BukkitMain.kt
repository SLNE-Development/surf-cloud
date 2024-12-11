package dev.slne.surf.cloud.bukkit

import com.destroystokyo.paper.event.player.PlayerHandshakeEvent
import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.cloud.api.common.exceptions.FatalSurfError
import dev.slne.surf.cloud.api.common.player.toCloudPlayer
import dev.slne.surf.cloud.api.common.server.serverManager
import dev.slne.surf.cloud.bukkit.player.BukkitClientCloudPlayerImpl
import dev.slne.surf.cloud.core.client.player.ClientCloudPlayerImpl
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.springframework.core.NestedRuntimeException

class BukkitMain : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {
        try {
            bukkitCloudInstance.onLoad()
        } catch (t: Throwable) {
            handleThrowable(t)
        }
    }

    override suspend fun onEnableAsync() {
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

        commandTree("getServer") {
            literalArgument("byId") {
                longArgument("id") {
                    anyExecutor { sender, args ->
                        val id: Long by args
                        GlobalScope.launch {
                            val server = serverManager.retrieveServerById(id)
                            sender.sendMessage("Server: $server")
                        }
                    }
                }
            }
            literalArgument("byCategoryAndName") {
                stringArgument("category") {
                    stringArgument("name") {
                        anyExecutor { sender, args ->
                            val category: String by args
                            val name: String by args
                            GlobalScope.launch {
                                val server =
                                    serverManager.retrieveServerByCategoryAndName(category, name)
                                sender.sendMessage("Server: $server")
                            }
                        }
                    }
                }
            }
            literalArgument("byName") {
                stringArgument("name") {
                    anyExecutor { sender, args ->
                        val name: String by args
                        GlobalScope.launch {
                            val server = serverManager.retrieveServerByName(name)
                            sender.sendMessage("Server: $server")
                        }
                    }
                }
            }
            literalArgument("byCategory") {
                stringArgument("category") {
                    anyExecutor { sender, args ->
                        val category: String by args
                        GlobalScope.launch {
                            val servers = serverManager.retrieveServersByCategory(category)
                            sender.sendMessage("Servers: $servers")
                        }
                    }
                }
            }
            literalArgument("self") {
                playerExecutor { player, _ ->
                    player.sendPlainMessage("Server: ${(player.toCloudPlayer()!! as BukkitClientCloudPlayerImpl).serverUid}")
                }
            }
        }

        commandAPICommand("changePlayers") {
            integerArgument("amount", min = 0)
            anyExecutor { sender, args ->
                val amount: Int by args
                Bukkit.setMaxPlayers(amount)
            }
        }
    }

    override suspend fun onDisableAsync() {
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

val plugin get() = BukkitMain.instance
