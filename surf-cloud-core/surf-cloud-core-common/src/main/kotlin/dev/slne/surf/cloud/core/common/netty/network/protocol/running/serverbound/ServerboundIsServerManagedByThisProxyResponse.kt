package dev.slne.surf.cloud.core.common.netty.network.protocol.running.serverbound

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

@SurfNettyPacket(
    DefaultIds.CLIENTBOUND_IS_SERVER_MANAGED_BY_THIS_PROXY_RESPONSE,
    PacketFlow.SERVERBOUND
)
class ServerboundIsServerManagedByThisProxyResponse : ResponseNettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(
            ServerboundIsServerManagedByThisProxyResponse::write,
            ::ServerboundIsServerManagedByThisProxyResponse
        )
    }

    val isManagedByThisProxy: Boolean

    constructor(isManagedByThisProxy: Boolean) {
        this.isManagedByThisProxy = isManagedByThisProxy
    }

    private constructor(buf: SurfByteBuf) {
        isManagedByThisProxy = buf.readBoolean()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeBoolean(isManagedByThisProxy)
    }
}