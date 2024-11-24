package dev.slne.surf.cloud.bukkit.netty

import dev.slne.surf.cloud.bukkit.netty.listener.NettyPlayerConnectionBlocker
import dev.slne.surf.cloud.bukkit.netty.task.ClientInformationUpdaterTask
import dev.slne.surf.cloud.core.client.netty.NettyCommonClientManager
import dev.slne.surf.surfapi.bukkit.api.event.register
import dev.slne.surf.surfapi.bukkit.api.event.unregister

object BukkitNettyManager : NettyCommonClientManager(false) {
    override suspend fun afterStart() {
        super.afterStart()
        ClientInformationUpdaterTask
    }

    override fun blockPlayerConnections() {
        NettyPlayerConnectionBlocker.register()
    }

    override fun unblockPlayerConnections() {
        NettyPlayerConnectionBlocker.unregister()
    }
}