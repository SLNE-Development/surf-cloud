package dev.slne.surf.cloud.bukkit.netty.network

import dev.slne.surf.cloud.core.client.netty.network.PlatformSpecificPacketListenerExtension
import java.net.InetSocketAddress

object BukkitSpecificPacketListenerExtension: PlatformSpecificPacketListenerExtension {
    override fun isServerManagedByThisProxy(address: InetSocketAddress): Boolean {
        error("Requested wrong server! This packet can only be acknowledged on a proxy!")
    }
}