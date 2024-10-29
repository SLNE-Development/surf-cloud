package dev.slne.surf.cloud.core.netty.network.protocol.running

import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.core.netty.network.ClientboundPacketListener

interface RunningClientPacketListener: ClientboundPacketListener {
    override val protocol get() = ConnectionProtocol.RUNNING
}