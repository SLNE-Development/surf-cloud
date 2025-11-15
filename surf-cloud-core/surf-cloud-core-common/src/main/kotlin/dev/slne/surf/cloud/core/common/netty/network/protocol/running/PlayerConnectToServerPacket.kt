package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.composite
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.player.task.PrePlayerJoinTask
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import net.kyori.adventure.nbt.CompoundBinaryTag
import java.net.Inet4Address
import java.util.*

/**
 * Sent by the client to connect to the standalone when the player is logging in on a proxy or bukkit server.
 *
 * The standalone will then add the player to their player list and broadcast the packet to all
 * other servers which then also add the player to their player list.
 */
@SurfNettyPacket(
    DefaultIds.PLAYER_CONNECT_TO_SERVER_PACKET,
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.DEFAULT
)
data class PlayerConnectToServerPacket(
    val uuid: UUID,
    val name: String,
    val serverName: String,
    val proxy: Boolean,
    val playerIp: Inet4Address
) : RespondingNettyPacket<PlayerConnectToServerResponsePacket>(),
    InternalNettyPacket<RunningServerPacketListener> {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            PlayerConnectToServerPacket::uuid,
            ByteBufCodecs.STRING_CODEC,
            PlayerConnectToServerPacket::name,
            ByteBufCodecs.STRING_CODEC,
            PlayerConnectToServerPacket::serverName,
            ByteBufCodecs.BOOLEAN_CODEC,
            PlayerConnectToServerPacket::proxy,
            ByteBufCodecs.INET_4_ADDRESS_CODEC,
            PlayerConnectToServerPacket::playerIp,
            ::PlayerConnectToServerPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handlePlayerConnectToServer(this)
    }
}

@SurfNettyPacket(
    DefaultIds.PLAYER_CONNECTED_TO_SERVER_PACKET,
    PacketFlow.CLIENTBOUND,
    handlerMode = PacketHandlerMode.DEFAULT
)
data class PlayerConnectedToServerPacket(
    val uuid: UUID,
    val name: String,
    val serverName: String,
    val proxy: Boolean,
    val playerIp: Inet4Address,
    val pdc: CompoundBinaryTag
) : NettyPacket(), InternalNettyPacket<RunningClientPacketListener> {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            PlayerConnectedToServerPacket::uuid,
            ByteBufCodecs.STRING_CODEC,
            PlayerConnectedToServerPacket::name,
            ByteBufCodecs.STRING_CODEC,
            PlayerConnectedToServerPacket::serverName,
            ByteBufCodecs.BOOLEAN_CODEC,
            PlayerConnectedToServerPacket::proxy,
            ByteBufCodecs.INET_4_ADDRESS_CODEC,
            PlayerConnectedToServerPacket::playerIp,
            ByteBufCodecs.COMPOUND_TAG_CODEC,
            PlayerConnectedToServerPacket::pdc,
            ::PlayerConnectedToServerPacket
        )
    }

    override fun handle(listener: RunningClientPacketListener) {
        listener.handlePlayerConnectedToServer(this)
    }
}

@SurfNettyPacket("cloud:response:connect_to_server", PacketFlow.BIDIRECTIONAL)
class PlayerConnectToServerResponsePacket(val result: PrePlayerJoinTask.Result) :
    ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            PrePlayerJoinTask.Result.STREAM_CODEC,
            PlayerConnectToServerResponsePacket::result,
            ::PlayerConnectToServerResponsePacket
        )
    }
}