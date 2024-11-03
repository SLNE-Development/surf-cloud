package dev.slne.surf.cloud.standalone.netty.server.network

import dev.slne.surf.cloud.core.netty.network.Connection
import dev.slne.surf.cloud.core.netty.network.ServerboundPacketListener
import dev.slne.surf.cloud.standalone.netty.server.ServerClientImpl


abstract class ServerCommonPacketListener(val client: ServerClientImpl, val connection: Connection): ServerboundPacketListener {




}

