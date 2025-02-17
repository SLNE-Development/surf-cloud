package dev.slne.surf.cloud.api.common.netty.packet

import dev.slne.surf.cloud.api.common.netty.network.Connection

@JvmInline
value class NettyPacketInfo(val origin: Connection)