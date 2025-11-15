package dev.slne.surf.cloud.core.common.netty.network.protocol.common

interface CommonClientSynchronizingRunningPacketListener :
    CommonSynchronizingRunningPacketListener {
    fun handleSetVelocitySecret(packet: ClientboundSetVelocitySecretPacket)
}