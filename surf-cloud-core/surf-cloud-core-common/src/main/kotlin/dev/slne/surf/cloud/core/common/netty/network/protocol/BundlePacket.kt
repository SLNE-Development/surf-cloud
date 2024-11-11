package dev.slne.surf.cloud.core.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket

abstract class BundlePacket protected constructor(val subPackets: Iterable<NettyPacket>): NettyPacket() {

}