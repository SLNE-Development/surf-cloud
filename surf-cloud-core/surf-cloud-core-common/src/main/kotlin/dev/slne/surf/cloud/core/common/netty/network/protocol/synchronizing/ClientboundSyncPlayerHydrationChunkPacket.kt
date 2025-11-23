package dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.composite
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.util.Either
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import net.kyori.adventure.nbt.CompoundBinaryTag
import java.net.Inet4Address
import java.util.*

@SurfNettyPacket(
    "cloud:clientbound:sync_players/chunk",
    PacketFlow.CLIENTBOUND,
    ConnectionProtocol.SYNCHRONIZING,
    handlerMode = PacketHandlerMode.NETTY
)
class ClientboundSyncPlayerHydrationChunkPacket(
    val entries: MutableList<Entry>
) : NettyPacket(), InternalNettyPacket<ClientSynchronizingPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            Entry.STREAM_CODEC.apply(ByteBufCodecs.list()),
            ClientboundSyncPlayerHydrationChunkPacket::entries,
            ::ClientboundSyncPlayerHydrationChunkPacket
        )
    }

    override fun handle(listener: ClientSynchronizingPacketListener) {
        listener.handleSyncPlayerHydrationChunk(this)
    }

    data class Entry(
        val uuid: UUID,
        val name: String,
        val serverName: String?,
        val proxyName: String?,
        val playerIp: Inet4Address,
        val pdcOrCallback: Either<CompoundBinaryTag, UUID>
    ) {
        companion object {
            val STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.UUID_CODEC,
                Entry::uuid,
                ByteBufCodecs.STRING_CODEC,
                Entry::name,
                ByteBufCodecs.STRING_CODEC.apply(ByteBufCodecs::nullable),
                Entry::serverName,
                ByteBufCodecs.STRING_CODEC.apply(ByteBufCodecs::nullable),
                Entry::proxyName,
                ByteBufCodecs.INET_4_ADDRESS_CODEC,
                Entry::playerIp,
                ByteBufCodecs.either(
                    ByteBufCodecs.COMPOUND_TAG_CODEC,
                    ByteBufCodecs.UUID_CODEC
                ),
                Entry::pdcOrCallback,
                ::Entry
            )
        }
    }
}