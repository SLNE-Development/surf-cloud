package dev.slne.surf.cloud.bukkit

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.CommandAPIPaper
import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.cloud.api.client.netty.packet.fireAndForget
import dev.slne.surf.cloud.api.common.TestPacket
import dev.slne.surf.cloud.api.common.player.teleport.TeleportCause
import dev.slne.surf.cloud.api.common.player.teleport.fineLocation
import dev.slne.surf.cloud.api.common.player.toCloudPlayer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.core.common.coreCloudInstance
import dev.slne.surf.cloud.core.common.handleEventuallyFatalError
import dev.slne.surf.surfapi.bukkit.api.event.listen
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.server.ServerLoadEvent
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class PaperMain : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {
        try {
            coreCloudInstance.onLoad()
        } catch (t: Throwable) {
            t.handleEventuallyFatalError { server.restart() }
        }
    }

    override suspend fun onEnableAsync() {
        try {
            coreCloudInstance.onEnable()
        } catch (t: Throwable) {
            t.handleEventuallyFatalError { server.restart() }
        }

        val serverLoaded = AtomicBoolean(false)
        listen<ServerLoadEvent> {
            if (!serverLoaded.compareAndSet(false, true)) return@listen

            launch {
                try {
                    coreCloudInstance.afterStart()
                } catch (t: Throwable) {
                    t.handleEventuallyFatalError { server.restart() }
                }
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
            stringArgument("serverName")
            anyExecutor { sender, args ->
                val serverName: String by args
                launch {
                    val server = CloudServerManager.retrieveServerByName(serverName)
                    requireCommand(server != null) { Component.text("Server '$serverName' not found") }

                    server.shutdown()
                }
            }
        }

        commandAPICommand("send-test-packet") {
            anyExecutor { sender, args ->
                TestPacket.random().fireAndForget()
                sender.sendPlainMessage("Test packet sent")
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
        throw CommandAPIPaper.failWithAdventureComponent(message)
    }

    override suspend fun onDisableAsync() {
        try {
            coreCloudInstance.onDisable()
        } catch (t: Throwable) {
            t.handleEventuallyFatalError {}
        }
    }

    val classLoader0: ClassLoader
        get() = classLoader

    companion object {
        @JvmStatic
        val instance: PaperMain
            get() = getPlugin(PaperMain::class.java)
    }
}

val plugin get() = PaperMain.instance
