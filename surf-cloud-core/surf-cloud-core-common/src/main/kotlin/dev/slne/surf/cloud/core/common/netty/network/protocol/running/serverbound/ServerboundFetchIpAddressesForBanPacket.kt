package dev.slne.surf.cloud.core.common.netty.network.protocol.running.serverbound

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.player.punishment.type.PunishmentAttachedIpAddress.PunishmentAttachedIpAddressImpl
import kotlinx.serialization.Serializable

@Serializable
@SurfNettyPacket("cloud:serverbound:fetch_ip_addresses_for_ban", PacketFlow.SERVERBOUND)
data class ServerboundFetchIpAddressesForBanPacket(val banId: Long) :
    RespondingNettyPacket<ClientboundFetchIpAddressesResponsePacket>()

@Serializable
@SurfNettyPacket("cloud:clientbound:fetch_ip_addresses_response", PacketFlow.CLIENTBOUND)
data class ClientboundFetchIpAddressesResponsePacket(val ipAddresses: List<PunishmentAttachedIpAddressImpl>) :
    ResponseNettyPacket()