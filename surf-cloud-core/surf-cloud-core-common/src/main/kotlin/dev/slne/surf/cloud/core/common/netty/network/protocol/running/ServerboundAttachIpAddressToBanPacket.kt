package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.boolean.BooleanResponsePacket
import kotlinx.serialization.Serializable

@Serializable
@SurfNettyPacket("cloud:serverbound:attach_ip_address_to_ban", PacketFlow.SERVERBOUND)
data class ServerboundAttachIpAddressToBanPacket(val banId: Long, val rawIp: String): BooleanResponsePacket()