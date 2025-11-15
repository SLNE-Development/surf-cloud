package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.util.Either
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import dev.slne.surf.cloud.core.common.player.whitelist.WhitelistEntryImpl
import java.util.*

@SurfNettyPacket(
    "cloud:serverbound:request_whitelist",
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.IO
)
class ServerboundRequestWhitelistPacket(
    val uuid: UUID,
    val groupOrServer: Either<String, String>
) : RespondingNettyPacket<WhitelistResponsePacket>(),
    InternalNettyPacket<RunningServerPacketListener> {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ServerboundRequestWhitelistPacket::uuid,
            WhitelistEntryImpl.GROUP_OR_SERVER_STREAM_CODEC,
            ServerboundRequestWhitelistPacket::groupOrServer,
            ::ServerboundRequestWhitelistPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleRequestWhitelist(this)
    }
}