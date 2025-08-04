package dev.slne.surf.cloud.api.client.velocity.server

import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import dev.slne.surf.cloud.api.common.server.CloudServer
import dev.slne.surf.cloud.api.common.server.CloudServerManager
import kotlin.jvm.optionals.getOrNull

fun CloudServer.toRegisteredServer(proxy: ProxyServer) =
    proxy.getServer(name).getOrNull() ?: error("Server $name is not registered in Velocity proxy")

fun RegisteredServer.toCloudServer() = CloudServerManager.retrieveServerByName(serverInfo.name)