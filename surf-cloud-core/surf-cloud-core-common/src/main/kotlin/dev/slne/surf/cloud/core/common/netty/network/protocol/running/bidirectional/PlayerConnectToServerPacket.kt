package dev.slne.surf.cloud.core.common.netty.network.protocol.running.bidirectional

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.player.task.PrePlayerJoinTask
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.net.Inet4Address
import java.util.*

/**
 * Sent by the client to connect to the standalone when the player is logging in on a proxy or bukkit server.
 *
 * The standalone will then add the player to their player list and broadcast the packet to all
 * other servers which than also add the player to their player list.
 *
 * @param uuid The uuid of the player
 * @param serverUid The uid of the server the player is connecting to
 * @param proxy If the server is a proxy
 */
@SurfNettyPacket(DefaultIds.PLAYER_CONNECT_TO_SERVER_PACKET, PacketFlow.SERVERBOUND)
@Serializable
data class PlayerConnectToServerPacket(
    val uuid: @Contextual UUID,
    val name: String,
    val serverUid: Long,
    val proxy: Boolean,
    val playerIp: @Contextual Inet4Address,
) : RespondingNettyPacket<PlayerConnectToServerResponsePacket>()

@SurfNettyPacket(DefaultIds.PLAYER_CONNECTED_TO_SERVER_PACKET, PacketFlow.CLIENTBOUND)
@Serializable
data class PlayerConnectedToServerPacket(
    val uuid: @Contextual UUID,
    val name: String,
    val serverUid: Long,
    val proxy: Boolean,
    val playerIp: @Contextual Inet4Address,
) : NettyPacket()

@SurfNettyPacket("cloud:response:connect_to_server", PacketFlow.BIDIRECTIONAL)
@Serializable
class PlayerConnectToServerResponsePacket(val result: PrePlayerJoinTask.Result) :
    ResponseNettyPacket()