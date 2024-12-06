package dev.slne.surf.cloud.bukkit.netty

import dev.slne.surf.cloud.bukkit.netty.listener.NettyPlayerConnectionBlocker
import dev.slne.surf.cloud.bukkit.netty.network.BukkitSpecificPacketListenerExtension
import dev.slne.surf.cloud.bukkit.netty.sync.ClientInformationUpdaterSyncer
import dev.slne.surf.cloud.core.client.netty.NettyCommonClientManager
import dev.slne.surf.surfapi.bukkit.api.event.register
import dev.slne.surf.surfapi.bukkit.api.event.unregister

object BukkitNettyManager : NettyCommonClientManager(false, BukkitSpecificPacketListenerExtension) {
    override suspend fun afterStart() {
        super.afterStart()
        ClientInformationUpdaterSyncer
    }

    override fun blockPlayerConnections() {
        NettyPlayerConnectionBlocker.register()
    }

    override fun unblockPlayerConnections() {
        NettyPlayerConnectionBlocker.unregister()
    }
}