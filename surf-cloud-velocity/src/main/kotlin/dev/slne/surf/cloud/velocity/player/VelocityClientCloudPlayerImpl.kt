package dev.slne.surf.cloud.velocity.player

import com.velocitypowered.api.proxy.Player
import dev.slne.surf.cloud.core.client.player.ClientCloudPlayerImpl
import dev.slne.surf.cloud.velocity.proxy
import java.util.*

class VelocityClientCloudPlayerImpl(uuid: UUID) : ClientCloudPlayerImpl<Player>(uuid) {
    override val platformClass = Player::class.java
    override val player: Player? get() = proxy.getPlayer(uuid).orElse(null)
}