package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CommonCloudServer
import dev.slne.surf.cloud.api.common.server.ProxyCloudServer
import java.net.InetSocketAddress

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_BATCH_UPDATE_SERVER,
    PacketFlow.CLIENTBOUND,
    ConnectionProtocol.SYNCHRONIZING
)
class ClientboundBatchUpdateServer(
    val servers: List<UpdateServerData>
) : NettyPacket() {

    companion object {
        val STREAM_CODEC =
            packetCodec(ClientboundBatchUpdateServer::write, ::ClientboundBatchUpdateServer)
    }

    constructor(servers: Iterable<CommonCloudServer>) : this(servers.map { server ->
        UpdateServerData(
            server.uid,
            server is ProxyCloudServer,
            (server as? CloudServer)?.lobby ?: false,
            server.group,
            server.name,
            server.playAddress
        )
    })

    private constructor(buf: SurfByteBuf) : this(buf.readList {
        UpdateServerData(
            buf.readVarLong(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readUtf(),
            buf.readUtf(),
            buf.readInetSocketAddress()
        )
    })

    private fun write(buf: SurfByteBuf) {
        buf.writeCollection(servers) { buf, data ->
            buf.writeVarLong(data.serverId)
            buf.writeBoolean(data.proxy)
            buf.writeBoolean(data.lobby)
            buf.writeUtf(data.group)
            buf.writeUtf(data.name)
            buf.writeInetSocketAddress(data.playAddress)
        }
    }
}

data class UpdateServerData(
    val serverId: Long,
    val proxy: Boolean,
    val lobby: Boolean,
    val group: String,
    val name: String,
    val playAddress: InetSocketAddress,
)