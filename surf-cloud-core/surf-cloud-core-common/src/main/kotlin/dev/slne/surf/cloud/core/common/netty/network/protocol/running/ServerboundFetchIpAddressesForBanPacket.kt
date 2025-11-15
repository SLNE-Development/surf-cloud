package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentAttachedIpAddress.PunishmentAttachedIpAddressImpl
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import kotlinx.serialization.Serializable

@SurfNettyPacket(
    "cloud:serverbound:fetch_ip_addresses_for_ban",
    PacketFlow.SERVERBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
data class ServerboundFetchIpAddressesForBanPacket(val banId: Long) :
    RespondingNettyPacket<ClientboundFetchIpAddressesResponsePacket>(),
    InternalNettyPacket<RunningServerPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.LONG_CODEC,
            ServerboundFetchIpAddressesForBanPacket::banId,
            ::ServerboundFetchIpAddressesForBanPacket
        )
    }

    override fun handle(listener: RunningServerPacketListener) {
        listener.handleFetchIpAddressesForBan(this)
    }
}

@Serializable
@SurfNettyPacket("cloud:clientbound:fetch_ip_addresses_response", PacketFlow.CLIENTBOUND)
data class ClientboundFetchIpAddressesResponsePacket(val ipAddresses: List<PunishmentAttachedIpAddressImpl>) :
    ResponseNettyPacket()