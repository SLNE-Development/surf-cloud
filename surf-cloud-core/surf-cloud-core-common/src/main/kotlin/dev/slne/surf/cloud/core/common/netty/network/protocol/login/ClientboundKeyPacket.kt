package dev.slne.surf.cloud.core.common.netty.network.protocol.login

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.common.util.encryption.Crypt

@SurfNettyPacket(DefaultIds.CLIENTBOUND_KEY_PACKET, PacketFlow.CLIENTBOUND, ConnectionProtocol.LOGIN)
class ClientboundKeyPacket : NettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(ClientboundKeyPacket::write, ::ClientboundKeyPacket)
    }

    val publicKey: ByteArray
    val challenge: ByteArray

    constructor(publicKey: ByteArray, challenge: ByteArray) {
        this.publicKey = publicKey
        this.challenge = challenge
    }

    private constructor(buf: SurfByteBuf) {
        publicKey = buf.readByteArray()
        challenge = buf.readByteArray()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeByteArray(publicKey)
        buf.writeByteArray(challenge)
    }

    fun decryptPublicKey() = Crypt.byteToPublicKey(publicKey)
}