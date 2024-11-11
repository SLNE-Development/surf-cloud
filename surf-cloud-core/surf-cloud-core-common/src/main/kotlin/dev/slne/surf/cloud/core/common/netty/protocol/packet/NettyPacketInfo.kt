package dev.slne.surf.cloud.core.common.netty.protocol.packet

data class NettyPacketInfo(
    val connection: Any, // TODO
    val proxiedSource: Any? // TODO
) {
    constructor(source: Any) : this(source, null)

//    @Contract(pure = true)
//    fun asClientSource() = source as NettyClientSource
//
//    @Contract(pure = true)
//    fun asServerSource() = source as NettyServerSource

}
