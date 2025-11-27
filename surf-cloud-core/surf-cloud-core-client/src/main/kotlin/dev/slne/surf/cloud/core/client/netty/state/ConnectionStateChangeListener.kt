package dev.slne.surf.cloud.core.client.netty.state

interface ConnectionStateChangeListener {
    fun onConnectionStateChanged(event: ConnectionEvent)
}