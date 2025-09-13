package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ClientboundPlayerCacheHydrateChunkPacket.Entry
import java.util.*

@SurfNettyPacket(
    "cloud:clientbound:player_cache/snapshot",
    PacketFlow.CLIENTBOUND
)
class ClientboundCacheSnapshotPacket(val uuid: UUID, val entries: List<Entry>) : ResponseNettyPacket()