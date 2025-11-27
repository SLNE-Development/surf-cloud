package dev.slne.surf.cloud.standalone.netty.server.network

import dev.slne.surf.cloud.core.common.netty.network.ServerboundPacketListener
import dev.slne.surf.cloud.core.common.netty.network.connection.ConnectionImpl
import dev.slne.surf.cloud.standalone.netty.server.ServerClientImpl


abstract class ServerCommonPacketListener(
    val client: ServerClientImpl,
    val connection: ConnectionImpl
) :
    ServerboundPacketListener

