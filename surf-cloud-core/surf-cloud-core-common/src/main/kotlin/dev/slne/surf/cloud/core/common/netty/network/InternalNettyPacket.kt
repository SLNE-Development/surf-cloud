package dev.slne.surf.cloud.core.common.netty.network

interface InternalNettyPacket<T : PacketListener> {

    fun handle(listener: T)
}