package dev.slne.surf.cloud.bukkit.player

import dev.slne.surf.cloud.api.client.netty.packet.fireAndForget
import dev.slne.surf.cloud.api.common.util.position.FineLocation
import dev.slne.surf.cloud.api.common.util.position.FineTeleportCause
import dev.slne.surf.cloud.api.common.util.position.FineTeleportFlag
import dev.slne.surf.cloud.bukkit.plugin
import dev.slne.surf.cloud.core.client.player.ClientCloudPlayerImpl
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.DisconnectPlayerPacket
import io.papermc.paper.entity.TeleportFlag
import kotlinx.coroutines.future.await
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.conversations.*
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.*

class BukkitClientCloudPlayerImpl(uuid: UUID) : ClientCloudPlayerImpl<Player>(uuid) {
    override val player: Player? get() = Bukkit.getPlayer(uuid)
    override val platformClass: Class<Player> = Player::class.java

    override fun disconnect(reason: Component) {
        DisconnectPlayerPacket(uuid, reason).fireAndForget()
    }

    fun test() {
        val player = player ?: return

        val factory = ConversationFactory(plugin)
        factory.withModality(true) // player dont receive messages from other players
        factory.withLocalEcho(false) // player dont see their own messages
        factory.withPrefix { ">> " } // prefix for messages
        factory.withTimeout(60) // timeout in seconds
        factory.withEscapeSequence("exit") // escape sequence to exit conversation
        val secondQuestion = object : NumericPrompt() {
            override fun acceptValidatedInput(
                context: ConversationContext,
                input: Number
            ): Prompt? {
                // process input

                return null // next prompt
            }

            override fun getPromptText(context: ConversationContext): String {
                return "Enter a number"
            }
        }

        val firstQuestion = object : BooleanPrompt() {

            override fun acceptValidatedInput(
                context: ConversationContext,
                input: Boolean
            ): Prompt? {
                // process input

                return secondQuestion // next prompt
            }

            override fun getPromptText(context: ConversationContext): String {
                return "Do you want to continue?"
            }
        }

        factory.withFirstPrompt(firstQuestion)

        val conversation = factory.buildConversation(player)
        conversation.begin()
    }

    override suspend fun teleport(
        location: FineLocation,
        teleportCause: FineTeleportCause,
        vararg flags: FineTeleportFlag
    ): Boolean {
        val player = player ?: return super.teleport(location, teleportCause, *flags)
        val world = Bukkit.getWorld(location.world) ?: return false
        val bukkitLocation =
            Location(world, location.x, location.y, location.z, location.yaw, location.pitch)

        val bukkitTeleportCause = when (teleportCause) {
            FineTeleportCause.PLUGIN -> PlayerTeleportEvent.TeleportCause.PLUGIN
            FineTeleportCause.COMMAND -> PlayerTeleportEvent.TeleportCause.COMMAND
            FineTeleportCause.UNKNOWN -> PlayerTeleportEvent.TeleportCause.UNKNOWN
            FineTeleportCause.DISMOUNT -> PlayerTeleportEvent.TeleportCause.UNKNOWN
            FineTeleportCause.EXIT_BED -> PlayerTeleportEvent.TeleportCause.UNKNOWN
            FineTeleportCause.SPECTATE -> PlayerTeleportEvent.TeleportCause.UNKNOWN
            FineTeleportCause.END_PORTAL -> PlayerTeleportEvent.TeleportCause.END_PORTAL
            FineTeleportCause.ENDER_PEARL -> PlayerTeleportEvent.TeleportCause.ENDER_PEARL
            FineTeleportCause.END_GATEWAY -> PlayerTeleportEvent.TeleportCause.END_GATEWAY
            FineTeleportCause.CHORUS_FRUIT -> PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT
            FineTeleportCause.NETHER_PORTAL -> PlayerTeleportEvent.TeleportCause.NETHER_PORTAL
        }

        val bukkitTeleportFlags = flags.map {
            when (it) {
                FineTeleportFlag.VELOCITY_ROTATION -> TeleportFlag.Relative.VELOCITY_ROTATION
                FineTeleportFlag.VELOCITY_X -> TeleportFlag.Relative.VELOCITY_X
                FineTeleportFlag.VELOCITY_Y -> TeleportFlag.Relative.VELOCITY_Y
                FineTeleportFlag.VELOCITY_Z -> TeleportFlag.Relative.VELOCITY_Z
                FineTeleportFlag.RETAIN_VEHICLE -> TeleportFlag.EntityState.RETAIN_VEHICLE
                FineTeleportFlag.RETAIN_PASSENGERS -> TeleportFlag.EntityState.RETAIN_PASSENGERS
                FineTeleportFlag.RETAIN_OPEN_INVENTORY -> TeleportFlag.EntityState.RETAIN_OPEN_INVENTORY
            }
        }.toTypedArray<TeleportFlag>()

        return player.teleportAsync(bukkitLocation, bukkitTeleportCause, *bukkitTeleportFlags)
            .await()
    }
}