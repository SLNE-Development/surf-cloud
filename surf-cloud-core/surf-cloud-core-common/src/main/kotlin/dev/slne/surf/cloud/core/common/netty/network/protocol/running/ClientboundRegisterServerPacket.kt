package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf

@SurfNettyPacket(DefaultIds.CLIENTBOUND_REGISTER_SERVER_PACKET, PacketFlow.CLIENTBOUND)
class ClientboundRegisterServerPacket: NettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(ClientboundRegisterServerPacket::write, ::ClientboundRegisterServerPacket)
    }

    val serverId: Long
    val proxy: Boolean
    val group: String
    val name: String

    constructor(serverId: Long, proxy: Boolean, group: String, name: String) {
        this.serverId = serverId
        this.proxy = proxy
        this.group = group
        this.name = name
    }

    private constructor(buf: SurfByteBuf) {
        serverId = buf.readVarLong()
        proxy = buf.readBoolean()
        group = buf.readUtf()
        name = buf.readUtf()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeVarLong(serverId)
        buf.writeBoolean(proxy)
        buf.writeUtf(group)
        buf.writeUtf(name)
    }
}