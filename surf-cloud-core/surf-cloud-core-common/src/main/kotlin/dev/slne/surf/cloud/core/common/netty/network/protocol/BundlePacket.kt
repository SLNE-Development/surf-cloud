package dev.slne.surf.cloud.core.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.util.toObjectSet

abstract class BundlePacket protected constructor(val subPackets: Iterable<NettyPacket>): NettyPacket() {
    init {
        val supportedProtocols = protocols.toObjectSet()
        for (packet in subPackets) {
            val packetProtocols = packet.protocols
            require(packetProtocols.any { it in supportedProtocols }) {
                "Packet ${packet.javaClass.simpleName} has unsupported protocols: $packetProtocols (supported: $supportedProtocols)"
            }
        }
    }
}