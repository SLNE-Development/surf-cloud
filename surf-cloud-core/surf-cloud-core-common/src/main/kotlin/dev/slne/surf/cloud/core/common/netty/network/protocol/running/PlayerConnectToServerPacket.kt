package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
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
@SurfNettyPacket(DefaultIds.PLAYER_CONNECT_TO_SERVER_PACKET, PacketFlow.BIDIRECTIONAL)
class PlayerConnectToServerPacket : NettyPacket {
    companion object {
        /**
         * Codec for encoding and decoding `PlayerConnectToServerPacket` packets.
         *
         * Utilizes the write method from `PlayerConnectToServerPacket` for encoding,
         * and the packet's constructor for decoding.
         *
         * @see PlayerConnectToServerPacket.write
         */
        val STREAM_CODEC =
            packetCodec(PlayerConnectToServerPacket::write, ::PlayerConnectToServerPacket)
    }

    /**
     * The UUID of the player.
     */
    val uuid: UUID

    /**
     * Represents the unique identifier of the server the player is connecting to.
     */
    val serverUid: Long

    /**
     * If the server is a proxy.
     */
    val proxy: Boolean

    /**
     * Constructs a new instance of `PlayerConnectToServerPacket`.
     *
     * @param uuid The UUID of the player.
     * @param serverUid The unique identifier of the server the player is connecting to.
     * @param proxy Indicates if the server is a proxy.
     */
    constructor(uuid: UUID, serverUid: Long, proxy: Boolean) {
        this.uuid = uuid
        this.serverUid = serverUid
        this.proxy = proxy
    }

    /**
     * Internal constructor that initializes a `PlayerConnectToServerPacket` instance
     * by reading from the provided `SurfByteBuf`.
     *
     * @param buf The buffer containing the serialized data for the packet.
     */
    private constructor(buf: SurfByteBuf) {
        this.uuid = buf.readUuid()
        this.serverUid = buf.readLong()
        this.proxy = buf.readBoolean()
    }

    /**
     * Writes the packet data to the provided buffer.
     *
     * @param buf The buffer into which the data should be written.
     */
    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(uuid)
        buf.writeLong(serverUid)
        buf.writeBoolean(proxy)
    }
}