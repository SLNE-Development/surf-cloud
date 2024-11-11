package dev.slne.surf.cloud.api.common.meta

import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import org.jetbrains.annotations.ApiStatus.Internal

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SurfNettyPacket(val id: Int, val flow: PacketFlow, @Internal val protocol: ConnectionProtocol = ConnectionProtocol.RUNNING)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class PacketCodec

object DefaultIds {
    const val PROXIED_NETTY_PACKET = 0x00

    // Handshake
    const val SERVERBOUND_HANDSHAKE_PACKET = 0x01


    // Initialize
    const val CLIENTBOUND_INITIALIZE_CLIENT_PACKET = 0x02

    const val SERVERBOUND_INITIALIZE_REQUEST_ID_PACKET = 0x03
    const val CLIENTBOUND_INITIALIZE_ID_RESPONSE = 0x04

    // Login
    const val SERVERBOUND_LOGIN_START_PACKET = 0x05
    const val CLIENTBOUND_LOGIN_FINISHED_PACKET = 0x06
    const val SERVERBOUND_LOGIN_ACKNOWLEDGED_PACKET = 0x07
    const val CLIENTBOUND_LOGIN_DISCONNECT_PACKET = 0x08

    // Running
    const val CLIENTBOUND_KEEP_ALIVE_PACKET = 0x09
    const val SERVERBOUND_KEEP_ALIVE_PACKET = 0x0A

    const val CLIENTBOUND_PING_PACKET = 0x0B
    const val SERVERBOUND_PONG_PACKET = 0x0C

    const val SERVERBOUND_PING_REQUEST_PACKET = 0x0D
    const val CLIENTBOUND_PING_REQUEST_RESPONSE_PACKET = 0x0E

    const val SERVERBOUND_BROADCAST_PACKET = 0x0F
    const val CLIENTBOUND_DISCONNECT_PACKET = 0x10

    const val SERVERBOUND_CLIENT_INFORMATION_PACKET = 0x11

    const val CLIENTBOUND_BUNDLE_DELIMITER_PACKET = 0x12
    const val CLIENTBOUND_BUNDLE_PACKET = 0x13
    const val SERVERBOUND_BUNDLE_DELIMITER_PACKET = 0x14
    const val SERVERBOUND_BUNDLE_PACKET = 0x15


    const val CLOUD_REGISTER_SERVER_PACKET = 0x16
    const val CLOUD_SERVER_INFO_PACKET = 0x17
    const val CLOUD_SERVER_INFO_BATCH_PACKET = 0x18
    const val CONTAINER_POST_CONNECTED = 0x19
    const val CLIENT_JOIN = 0x1A
    const val CLIENT_QUIT = 0x1B
}
