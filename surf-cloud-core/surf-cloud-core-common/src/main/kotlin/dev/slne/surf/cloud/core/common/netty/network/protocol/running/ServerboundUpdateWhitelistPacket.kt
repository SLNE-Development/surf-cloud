package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.boolean.BooleanResponsePacket
import dev.slne.surf.cloud.core.common.player.whitelist.MutableWhitelistEntryImpl
import kotlinx.serialization.Serializable

@SurfNettyPacket("cloud:serverbound:update_whitelist", PacketFlow.SERVERBOUND)
@Serializable
class ServerboundUpdateWhitelistPacket(val updated: MutableWhitelistEntryImpl) :
    BooleanResponsePacket()