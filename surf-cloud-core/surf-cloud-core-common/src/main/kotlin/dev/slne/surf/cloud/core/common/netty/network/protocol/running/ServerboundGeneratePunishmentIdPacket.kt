package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.string.StringResponsePacket
import kotlinx.serialization.Serializable

@Serializable
@SurfNettyPacket("cloud:serverbound:generate_punishment_id", PacketFlow.SERVERBOUND)
class ServerboundGeneratePunishmentIdPacket: StringResponsePacket()