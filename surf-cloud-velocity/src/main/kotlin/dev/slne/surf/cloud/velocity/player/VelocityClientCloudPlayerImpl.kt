package dev.slne.surf.cloud.velocity.player

import com.velocitypowered.api.proxy.Player
import dev.slne.surf.cloud.core.client.player.ClientCloudPlayerImpl
import dev.slne.surf.cloud.velocity.proxy
import net.kyori.adventure.text.Component
import java.util.*

class VelocityClientCloudPlayerImpl(uuid: UUID) : ClientCloudPlayerImpl<Player>(uuid) {
    override val platformClass = Player::class.java
    override val audience: Player? get() = proxy.getPlayer(uuid).orElse(null)
    override fun disconnect(reason: Component) {
        audience?.disconnect(reason)
    }
}