package dev.slne.surf.cloud.api.client.server

import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi

interface CloudClientServerManager : CloudServerManager {
    fun currentServer(): CloudServer

    @OptIn(InternalApi::class)
    companion object :
        CloudClientServerManager by CloudServerManager.instance as CloudClientServerManager
}