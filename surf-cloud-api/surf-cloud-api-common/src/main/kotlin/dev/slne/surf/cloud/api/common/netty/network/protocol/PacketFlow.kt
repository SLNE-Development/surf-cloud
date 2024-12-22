package dev.slne.surf.cloud.api.common.netty.network.protocol

enum class PacketFlow {
    CLIENTBOUND,
    SERVERBOUND,
    BIDIRECTIONAL;

    fun getOpposite(): PacketFlow {
        if (this == BIDIRECTIONAL) throw IllegalStateException("Cannot get opposite of BOTH")
        return if (this == CLIENTBOUND) SERVERBOUND else CLIENTBOUND
    }

    fun displayName(): String {
        return when (this) {
            CLIENTBOUND -> "C -> S"
            SERVERBOUND -> "S -> C"
            BIDIRECTIONAL -> "C <-> S"
        }
    }
}