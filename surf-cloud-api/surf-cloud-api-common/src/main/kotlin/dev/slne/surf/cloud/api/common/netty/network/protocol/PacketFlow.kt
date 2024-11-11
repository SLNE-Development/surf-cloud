package dev.slne.surf.cloud.api.common.netty.network.protocol

enum class PacketFlow {
    CLIENTBOUND,
    SERVERBOUND;

    fun getOpposite() = if (this == CLIENTBOUND) SERVERBOUND else CLIENTBOUND
}