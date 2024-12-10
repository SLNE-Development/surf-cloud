package dev.slne.surf.cloud.core.common.netty.network.protocol.login

import dev.slne.surf.cloud.api.common.meta.DefaultIds
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.packetCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.common.util.encryption.Crypt
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.SecretKey

@SurfNettyPacket(
    DefaultIds.SERVERBOUND_KEY_PACKET,
    PacketFlow.SERVERBOUND,
    ConnectionProtocol.LOGIN
)
class ServerboundKeyPacket: NettyPacket {

    companion object {
        val STREAM_CODEC = packetCodec(ServerboundKeyPacket::write, ::ServerboundKeyPacket)
    }

    val keyBytes: ByteArray
    val encryptedChallenge: ByteArray

    constructor(secretKey: SecretKey, publicKey: PublicKey, nonce: ByteArray) {
        this.keyBytes = Crypt.encryptUsingKey(publicKey, secretKey.encoded)
        this.encryptedChallenge = Crypt.encryptUsingKey(publicKey, nonce)
    }

    private constructor(buf: SurfByteBuf) {
        this.keyBytes = buf.readByteArray()
        this.encryptedChallenge = buf.readByteArray()
    }

    private fun write(buf: SurfByteBuf) {
        buf.writeByteArray(keyBytes)
        buf.writeByteArray(encryptedChallenge)
    }

    fun getSecretKey(privateKey: PrivateKey): SecretKey {
        return Crypt.decryptByteToSecretKey(privateKey, keyBytes)
    }

    fun isChallengeValid(nonce: ByteArray, privateKey: PrivateKey): Boolean {
        return try {
            nonce.contentEquals(Crypt.decryptUsingKey(privateKey, this.encryptedChallenge))
        } catch (_: Exception) {
            false
        }
    }
}