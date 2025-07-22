package dev.slne.surf.cloud.api.client.velocity.server

import com.velocitypowered.api.proxy.ProxyServer
import dev.slne.surf.cloud.api.common.server.CloudServer
import kotlin.jvm.optionals.getOrNull

fun CloudServer.toRegisteredServer(proxy: ProxyServer) =
    proxy.getServer(name).getOrNull() ?: error("Server $name is not registered in Velocity proxy")