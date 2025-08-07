package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.core.common.player.whitelist.WhitelistEntryImpl
import kotlinx.serialization.Serializable

@SurfNettyPacket("cloud:serverbound:create_whitelist", PacketFlow.SERVERBOUND)
@Serializable
class ServerboundCreateWhitelistPacket(val entry: WhitelistEntryImpl) :
    RespondingNettyPacket<WhitelistResponsePacket>()