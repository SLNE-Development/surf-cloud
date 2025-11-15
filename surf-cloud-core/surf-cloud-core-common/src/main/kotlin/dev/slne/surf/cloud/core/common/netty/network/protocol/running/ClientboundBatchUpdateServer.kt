package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.composite
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CommonCloudServer
import dev.slne.surf.cloud.api.common.server.ProxyCloudServer
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.ClientSynchronizingPacketListener
import dev.slne.surf.surfapi.core.api.util.collectionSizeOrDefault
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf
import java.net.InetSocketAddress

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_BATCH_UPDATE_SERVER,
    PacketFlow.CLIENTBOUND,
    ConnectionProtocol.SYNCHRONIZING,
    handlerMode = PacketHandlerMode.NETTY
)
class ClientboundBatchUpdateServer(
    val servers: MutableList<UpdateServerData>
) : NettyPacket(), InternalNettyPacket<ClientSynchronizingPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            UpdateServerData.STREAM_CODEC.apply(ByteBufCodecs.list()),
            ClientboundBatchUpdateServer::servers,
            ::ClientboundBatchUpdateServer
        )
    }

    constructor(servers: Iterable<CommonCloudServer>) : this(
        servers.mapTo(
            mutableObjectListOf(servers.collectionSizeOrDefault(10)),
            UpdateServerData::fromServer
        )
    )

    override fun handle(listener: ClientSynchronizingPacketListener) {
        listener.handleBatchUpdateServer(this)
    }

    data class UpdateServerData(
        val proxy: Boolean,
        val lobby: Boolean,
        val group: String,
        val name: String,
        val playAddress: InetSocketAddress,
    ) {
        companion object {
            val STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOLEAN_CODEC,
                UpdateServerData::proxy,
                ByteBufCodecs.BOOLEAN_CODEC,
                UpdateServerData::lobby,
                ByteBufCodecs.STRING_CODEC,
                UpdateServerData::group,
                ByteBufCodecs.STRING_CODEC,
                UpdateServerData::name,
                ByteBufCodecs.INET_SOCKET_ADDRESS_CODEC,
                UpdateServerData::playAddress,
                ::UpdateServerData
            )

            fun fromServer(server: CommonCloudServer) = UpdateServerData(
                server is ProxyCloudServer,
                (server as? CloudServer)?.lobby ?: false,
                server.group,
                server.name,
                server.playAddress
            )
        }
    }
}
