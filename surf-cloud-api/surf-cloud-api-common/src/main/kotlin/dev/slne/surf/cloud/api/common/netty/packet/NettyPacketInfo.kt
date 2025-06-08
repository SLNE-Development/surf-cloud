package dev.slne.surf.cloud.api.common.netty.packet

import dev.slne.surf.cloud.api.common.netty.network.Connection
import dev.slne.surf.cloud.api.common.netty.network.ConnectionProtocol

data class NettyPacketInfo(val origin: Connection, val protocol: ConnectionProtocol)