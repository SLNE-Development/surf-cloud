package dev.slne.surf.cloud.api.client.server

import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.api.common.server.CommonCloudServer
import dev.slne.surf.cloud.api.common.server.ProxyCloudServer
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi

interface CloudClientServerManager : CloudServerManager {
    fun current(): CommonCloudServer
    fun currentServer(): CloudServer
    fun currentProxy(): ProxyCloudServer

    @OptIn(InternalApi::class)
    companion object :
        CloudClientServerManager by CloudServerManager.instance as CloudClientServerManager
}

fun CommonCloudServer.Companion.current(): CommonCloudServer {
    return CloudClientServerManager.current()
}

fun CloudServer.Companion.current(): CloudServer {
    return CloudClientServerManager.currentServer()
}

fun ProxyCloudServer.Companion.current(): ProxyCloudServer {
    return CloudClientServerManager.currentProxy()
}