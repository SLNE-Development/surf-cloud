package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import kotlinx.serialization.Serializable

@SurfNettyPacket("cloud:clientbound:set_velocity_secret", PacketFlow.CLIENTBOUND)
@Serializable
class ClientboundSetVelocitySecretPacket(val secret: ByteArray): NettyPacket() {
    override fun toString(): String {
        return "ClientboundSetVelocitySecretPacket(secret=***)"
    }
}