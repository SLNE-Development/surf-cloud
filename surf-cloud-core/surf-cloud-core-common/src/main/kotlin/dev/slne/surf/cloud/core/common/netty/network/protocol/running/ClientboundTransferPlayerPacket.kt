package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacketHandler
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import java.net.InetSocketAddress
import java.util.*

@SurfNettyPacket(DefaultIds.CLIENTBOUND_TRANSFER_PLAYER_PACKET, PacketFlow.CLIENTBOUND)
class ClientboundTransferPlayerPacket :
    RespondingNettyPacket<ServerboundTransferPlayerPacketResponse> {

    companion object {
        val STREAM_CODEC =
            packetCodec(ClientboundTransferPlayerPacket::write, ::ClientboundTransferPlayerPacket)
    }

    val playerUuid: UUID
    val address: InetSocketAddress

    constructor(playerUuid: UUID, address: InetSocketAddress) {
        this.playerUuid = playerUuid
        this.address = address
    }

    private constructor(buf: SurfByteBuf) {
        playerUuid = buf.readUuid()
        address = buf.readInetSocketAddress()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeUuid(playerUuid)
        buf.writeInetSocketAddress(address)
    }
}