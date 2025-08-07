package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.util.Either
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@SurfNettyPacket("cloud:serverbound:request_whitelist", PacketFlow.SERVERBOUND)
@Serializable
class ServerboundRequestWhitelistPacket(
    val uuid: @Contextual UUID,
    val groupOrServer: Either<String, String>
) : RespondingNettyPacket<WhitelistResponsePacket>()