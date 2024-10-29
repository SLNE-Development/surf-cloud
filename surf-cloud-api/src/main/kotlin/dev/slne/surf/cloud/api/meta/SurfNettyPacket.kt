package dev.slne.surf.cloud.api.meta

import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.netty.network.protocol.PacketFlow
import org.jetbrains.annotations.ApiStatus.Internal

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SurfNettyPacket(val id: Int, val flow: PacketFlow, @Internal val protocol: ConnectionProtocol = ConnectionProtocol.RUNNING)

object DefaultIds {
    const val PROXIED_NETTY_PACKET = 0x00

    // Handshake
    const val SERVERBOUND_HANDSHAKE_PACKET = 0x01


    // Initialize
    const val CLIENTBOUND_INITIALIZE_CLIENT_PACKET = 0x02


    // Login
    const val SERVERBOUND_LOGIN_START_PACKET = 0x03
    const val CLIENTBOUND_LOGIN_FINISHED_PACKET = 0x04
    const val SERVERBOUND_LOGIN_ACKNOWLEDGED_PACKET = 0x05
    const val CLIENTBOUND_LOGIN_DISCONNECT_PACKET = 0x06

    // Running
    const val CLIENTBOUND_KEEP_ALIVE_PACKET = 0x07
    const val SERVERBOUND_KEEP_ALIVE_PACKET = 0x08

    const val CLIENTBOUND_PING_PACKET = 0x09
    const val SERVERBOUND_PONG_PACKET = 0x0A


    const val CLOUD_REGISTER_SERVER_PACKET = 0x0C
    const val CLOUD_SERVER_INFO_PACKET = 0x0D
    const val CLOUD_SERVER_INFO_BATCH_PACKET = 0x0E
    const val CONTAINER_POST_CONNECTED = 0x0F
    const val CLIENT_JOIN = 0x10
    const val CLIENT_QUIT = 0x11
}
