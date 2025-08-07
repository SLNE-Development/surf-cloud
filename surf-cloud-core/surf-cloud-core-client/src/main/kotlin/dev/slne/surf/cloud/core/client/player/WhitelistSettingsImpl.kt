package dev.slne.surf.cloud.core.client.player

import dev.slne.surf.cloud.api.client.netty.packet.fireAndForget
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundRefreshWhitelistPacket
import dev.slne.surf.cloud.core.common.player.whitelist.AbstractWhitelistSettings
import org.springframework.stereotype.Component

@Component
class WhitelistSettingsImpl : AbstractWhitelistSettings() {
    override fun refresh() {
        ServerboundRefreshWhitelistPacket.fireAndForget()
    }
}