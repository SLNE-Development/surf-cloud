package dev.slne.surf.cloud.standalone.netty.server

import dev.slne.surf.cloud.core.netty.CommonNettyClientImpl
import dev.slne.surf.cloud.standalone.netty.server.network.ServerRunningPacketListenerImpl

class ServerClientImpl(serverId: Long, serverCategory: String) :
    CommonNettyClientImpl(serverId, serverCategory) {

    private var _listener: ServerRunningPacketListenerImpl? = null
        set(value) {
            field = value
            if (value != null) {
                initConnection(value.connection)
            }
        }
    val listener get() = _listener ?: error("listener not yet set")


    val displayName get() = "${serverCategory}/${serverId} (${host})"

    fun initListener(listener: ServerRunningPacketListenerImpl) {
        _listener = listener
    }
}