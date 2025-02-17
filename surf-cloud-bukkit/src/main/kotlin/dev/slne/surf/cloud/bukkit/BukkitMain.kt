package dev.slne.surf.cloud.bukkit

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.ticks
import dev.jorel.commandapi.CommandAPIBukkit
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.executors.NativeCommandExecutor
import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.cloud.api.common.player.teleport.TeleportCause
import dev.slne.surf.cloud.api.common.player.teleport.fineLocation
import dev.slne.surf.cloud.api.common.player.toCloudPlayer
import dev.slne.surf.cloud.api.common.server.serverManager
import dev.slne.surf.cloud.bukkit.player.BukkitClientCloudPlayerImpl
import dev.slne.surf.cloud.core.common.handleEventuallyFatalError
import dev.slne.surf.surfapi.bukkit.api.event.listen
import kotlinx.coroutines.delay
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.server.ServerLoadEvent
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class BukkitMain : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {
        try {
            bukkitCloudInstance.onLoad()
        } catch (t: Throwable) {
            t.handleEventuallyFatalError({ Bukkit.shutdown() })
        }
    }

    override suspend fun onEnableAsync() {
        try {
            bukkitCloudInstance.onEnable()
        } catch (t: Throwable) {
            t.handleEventuallyFatalError({ Bukkit.shutdown() })
        }

        var serverLoaded = false
        listen<ServerLoadEvent> {
            if (serverLoaded) {
                return@listen
            }

            serverLoaded = true
            System.err.println("Server loaded ##############################################")
        }

        // TODO: does this actually delay until the server is fully loaded?
        launch(globalRegionDispatcher) {
            delay(1.ticks)
            try {
                bukkitCloudInstance.afterStart()
            } catch (t: Throwable) {
                t.handleEventuallyFatalError({ Bukkit.shutdown() })
            }
        }

        commandTree("getServer") {
            literalArgument("byId") {
                longArgument("id") {
                    anyExecutor { sender, args ->
                        val id: Long by args
                        launch {
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
                            launch {
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
                        launch {
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
                        launch {
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

        commandAPICommand("setPdc") {
            namespacedKeyArgument("key")
            stringArgument("value")

            playerExecutor { player, args ->
                val key: NamespacedKey by args
                val value: String by args

                launch {
                    player.toCloudPlayer()!!.withPersistentData {
                        setString(key, value)
                    }
                }
            }
        }

        commandAPICommand("disconnect") {
            entitySelectorArgumentOnePlayer("target")
            adventureChatComponentArgument("reason", optional = true)

            playerExecutor { player, args ->
                val target: Player by args
                val reason =
                    args.getOptionalUnchecked<Component>("reason").orElse(Component.empty())

                target.toCloudPlayer()?.disconnect(reason)
                player.sendMessage(
                    Component.text(
                        "Disconnected player ${target.name}",
                        NamedTextColor.GREEN
                    )
                )
            }
        }

        commandAPICommand("deport") {
            entitySelectorArgumentOnePlayer("target")
            locationArgument("location")

            playerExecutor { player, args ->
                val target: Player by args
                val location: Location by args

                val fineLocation = fineLocation(
                    location.world.uid,
                    location.x,
                    location.y,
                    location.z,
                    location.yaw,
                    location.pitch
                )

                plugin.launch {
                    target.toCloudPlayer()?.teleport(fineLocation, TeleportCause.COMMAND)

                    player.sendMessage(
                        Component.text(
                            "Deported player ${target.name} to $location",
                            NamedTextColor.GREEN
                        )
                    )
                }
            }
        }

        commandAPICommand("cshutdown") {
            longArgument("id")
            anyExecutor { sender, args ->
                val id: Long by args
                launch {
                    val server = serverManager.retrieveServerById(id)
                    requireCommand(server != null) { Component.text("Server with id $id not found") }

                    server.shutdown()
                }
            }
        }
    }

    @OptIn(ExperimentalContracts::class)
    private fun requireCommand(
        condition: Boolean,
        message: () -> Component
    ) { // TODO: 20.01.2025 20:51 - move to surf-api
        contract {
            returns() implies condition
        }

        if (!condition) {
            failCommand(message)
        }
    }

    private fun failCommand(message: () -> Component): Nothing { // TODO: 20.01.2025 20:51 - move to surf-api
        throw CommandAPIBukkit.failWithAdventureComponent(message)
    }

    override suspend fun onDisableAsync() {
        try {
            bukkitCloudInstance.onDisable()
        } catch (t: Throwable) {
            t.handleEventuallyFatalError({ })
        }
    }

    val classLoader0: ClassLoader
        get() = classLoader

    companion object {
        @JvmStatic
        val instance: BukkitMain
            get() = getPlugin(BukkitMain::class.java)
    }
}

val plugin get() = BukkitMain.instance
