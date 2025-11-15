package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket

@SurfNettyPacket(
    "cloud:clientbound:set_velocity_secret",
    PacketFlow.CLIENTBOUND,
    handlerMode = PacketHandlerMode.NETTY
)
class ClientboundSetVelocitySecretPacket(val secret: ByteArray) : NettyPacket(),
    InternalNettyPacket<CommonClientSynchronizingRunningPacketListener> {

    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE_ARRAY_CODEC,
            ClientboundSetVelocitySecretPacket::secret,
            ::ClientboundSetVelocitySecretPacket
        )
    }

    override fun handle(listener: CommonClientSynchronizingRunningPacketListener) {
        listener.handleSetVelocitySecret(this)
    }

    override fun toString(): String {
        return "ClientboundSetVelocitySecretPacket(secret=***)"
    }
}