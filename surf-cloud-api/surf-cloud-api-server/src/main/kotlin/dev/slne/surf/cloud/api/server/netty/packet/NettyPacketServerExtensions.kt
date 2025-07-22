package dev.slne.surf.cloud.api.server.netty.packet

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.server.server.ServerCloudServerManager


fun NettyPacket.broadcast() {
    ServerCloudServerManager.broadcast(this)
}