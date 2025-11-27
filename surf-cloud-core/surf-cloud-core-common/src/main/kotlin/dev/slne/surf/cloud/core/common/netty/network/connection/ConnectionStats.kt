package dev.slne.surf.cloud.core.common.netty.network.connection

import dev.slne.surf.cloud.api.common.util.math.lerp

class ConnectionStats(private val connectionImpl: ConnectionImpl) {

    var packetsReceived = 0
        private set
    var receivedPackets = 0
        private set
    var sentPackets = 0
        private set
    var averageReceivedPackets = 0f
        private set
    var averageSentPackets = 0f
        private set

    var latency: Int = 0


    fun onPacketReceived() {
        packetsReceived++
        receivedPackets++
    }

    fun onPacketSent() {
        sentPackets++
    }

    fun recalculateAverages() {
        this.averageSentPackets = lerp(
            0.75f, this.sentPackets.toFloat(),
            this.averageSentPackets
        )
        this.averageReceivedPackets = lerp(
            0.75f, this.receivedPackets.toFloat(),
            this.averageReceivedPackets
        )
        this.sentPackets = 0
        this.receivedPackets = 0
    }
}