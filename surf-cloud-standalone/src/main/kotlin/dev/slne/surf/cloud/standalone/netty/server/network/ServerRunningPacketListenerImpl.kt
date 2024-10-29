package dev.slne.surf.cloud.standalone.netty.server.network

import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.util.logger
import dev.slne.surf.cloud.core.coroutines.NettyListenerScope
import dev.slne.surf.cloud.core.netty.client.source.NettyClientSourceImpl
import dev.slne.surf.cloud.core.netty.common.registry.listener.NettyListenerRegistry
import dev.slne.surf.cloud.core.netty.network.Connection
import dev.slne.surf.cloud.core.netty.network.protocol.running.RunningServerPacketListener
import dev.slne.surf.cloud.core.netty.protocol.packet.NettyPacketInfo
import dev.slne.surf.cloud.core.netty.protocol.packets.ProxiedNettyPacket
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ServerRunningPacketListenerImpl(connection: Connection) : ServerCommonPacketListener(connection), RunningServerPacketListener {
   private val log = logger()

    override fun handlePacket(packet: NettyPacket) {
        val listeners = NettyListenerRegistry.getListeners(packet.javaClass) ?: return
        if (listeners.isEmpty()) return

        val (proxiedSource, finalPacket) = when (packet) {
            is ProxiedNettyPacket -> packet.source to packet.packet
            else -> null to packet
        }
        val info = NettyPacketInfo(this, proxiedSource)

        for (listener in listeners) {
            NettyListenerScope.launch {
                try {
                    listener.handle(finalPacket, info)
                } catch (e: Exception) {
                    log.atWarning()
                        .withCause(e)
                        .atMostEvery(5, TimeUnit.SECONDS)
                        .log(
                            "Failed to call listener %s for packet %s",
                            listener::class.simpleName,
                            finalPacket::class.simpleName
                        )
                }
            }
        }
    }
}