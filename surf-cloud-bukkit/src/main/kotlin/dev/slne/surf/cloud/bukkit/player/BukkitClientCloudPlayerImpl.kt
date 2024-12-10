package dev.slne.surf.cloud.bukkit.player

import dev.slne.surf.cloud.bukkit.plugin
import dev.slne.surf.cloud.core.client.player.ClientCloudPlayerImpl
import net.kyori.adventure.audience.Audience.audience
import org.bukkit.Bukkit
import org.bukkit.conversations.*
import org.bukkit.entity.Player
import java.util.*

class BukkitClientCloudPlayerImpl(uuid: UUID) : ClientCloudPlayerImpl<Player>(uuid) {
    override val player: Player? get() = Bukkit.getPlayer(uuid)
    override val platformClass: Class<Player> = Player::class.java

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
}