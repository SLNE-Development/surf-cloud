package dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.kyori.adventure.key.Key

/**
 * Packet sent by the client to register all cache keys known to the client.
 * Responds with [ClientboundCacheRegisterAckPacket].
 */
@SurfNettyPacket(
    "cloud:serverbound:cache/register_keys",
    PacketFlow.SERVERBOUND,
    ConnectionProtocol.SYNCHRONIZING
)
@Serializable
class ServerboundCacheRegisterKeysPacket(val keys: List<Pair<@Contextual Key, Boolean>>) :
    NettyPacket()