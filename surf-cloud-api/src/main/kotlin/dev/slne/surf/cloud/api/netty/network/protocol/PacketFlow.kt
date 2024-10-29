package dev.slne.surf.cloud.api.netty.network.protocol

enum class PacketFlow {
    CLIENTBOUND,
    SERVERBOUND;

    fun getOpposite() = if (this == CLIENTBOUND) SERVERBOUND else CLIENTBOUND
}