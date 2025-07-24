package dev.slne.surf.cloud.core.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf

interface BundlerInfo {
    companion object {
        const val BUNDLE_SIZE_LIMIT = 0x1000

        fun <P : BundlePacket> createForPacket(
            id: Class<P>,
            bundleFunction: (Iterable<NettyPacket>) -> P,
            splitter: BundleDelimiterPacket
        ): BundlerInfo {
            return object : BundlerInfo {
                @Suppress("UNCHECKED_CAST")
                override fun unbundlePacket(packet: NettyPacket, consumer: (NettyPacket) -> Unit) {
                    if (packet::class.java == id) {
                        val bundlerPacket = packet as? P ?: error("Invalid packet type")

                        consumer(splitter)
                        bundlerPacket.subPackets.forEach(consumer)
                        consumer(splitter)
                    } else {
                        consumer(packet)
                    }
                }

                override fun startPacketBundling(splitter0: NettyPacket): Bundler? {
                    return if (splitter === splitter0) object : Bundler {
                        private val bundlePackets = mutableObjectListOf<NettyPacket>()

                        override fun addPacket(packet: NettyPacket): NettyPacket? {
                            if (packet === splitter0) {
                                return bundleFunction(this.bundlePackets)
                            } else check(bundlePackets.size < BUNDLE_SIZE_LIMIT) { "Too many packets in a bundle" }
                            bundlePackets.add(packet)
                            return null
                        }
                    } else null
                }
            }
        }
    }

    fun unbundlePacket(packet: NettyPacket, consumer: (NettyPacket) -> Unit)

    fun startPacketBundling(splitter: NettyPacket): Bundler?

    interface Bundler {
        fun addPacket(packet: NettyPacket): NettyPacket?
    }
}