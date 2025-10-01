package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import java.util.*

/**
 * Eröffnet die Initialübertragung eines gesamten Player-Caches (Hydration).
 * Wird vom Server an den Client gesendet, bevor die einzelnen Chunk-Pakete geschickt werden.
 */
@SurfNettyPacket(
    "cloud:clientbound:player_cache/hydrate_start",
    PacketFlow.CLIENTBOUND
)
class ClientboundPlayerCacheHydrateStartPacket(val uuid: UUID) : NettyPacket()