package dev.slne.surf.cloud.standalone.netty.server.source.client

import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.util.mutableObjectListOf
import dev.slne.surf.cloud.api.util.synchronize
import dev.slne.surf.cloud.standalone.netty.server.network.ServerRunningPacketListenerImpl

class ServerClientSource(
    val serverGuid: Long
) {

    private val packetQueue by lazy { mutableObjectListOf<NettyPacket<*>>().synchronize() }
    private var _connection: ServerRunningPacketListenerImpl? = null
        set(value) {
            field = value

            if (value != null) {
                synchronized(packetQueue) {
                    packetQueue.forEach { value.connection.send(it) }
                    packetQueue.clear()
                }
            }
        }

    val connection get() = _connection ?: error("connection not set")


    // region api
    fun sendPacket(packet: NettyPacket<*>) {
        val connection = _connection
        if (connection == null) {
            packetQueue.add(packet)
        } else {
            connection.connection.send(packet)
        }
    }
    // endregion api
}