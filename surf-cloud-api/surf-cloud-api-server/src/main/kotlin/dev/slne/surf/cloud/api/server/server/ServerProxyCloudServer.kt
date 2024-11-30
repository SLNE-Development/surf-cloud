package dev.slne.surf.cloud.api.server.server

import dev.slne.surf.cloud.api.common.server.ProxyCloudServer
import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
interface ServerProxyCloudServer: ServerCommonCloudServer, ProxyCloudServer