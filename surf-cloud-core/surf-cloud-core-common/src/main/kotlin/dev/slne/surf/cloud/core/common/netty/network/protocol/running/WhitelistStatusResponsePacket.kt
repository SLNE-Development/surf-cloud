package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.player.whitelist.WhitelistStatus
import kotlinx.serialization.Serializable

@SurfNettyPacket("cloud:bidirectional:whitelist_status_response", PacketFlow.BIDIRECTIONAL)
@Serializable
class WhitelistStatusResponsePacket(val status: WhitelistStatus) : ResponseNettyPacket()