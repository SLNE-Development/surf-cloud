package dev.slne.surf.cloud.standalone.netty.server.network

import dev.slne.surf.cloud.core.netty.network.Connection
import dev.slne.surf.cloud.core.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.netty.network.protocol.initialize.ClientboundInitializeIdResponsePacket
import dev.slne.surf.cloud.core.netty.network.protocol.initialize.ServerInitializePacketListener
import dev.slne.surf.cloud.core.netty.network.protocol.initialize.ServerboundInitializeRequestIdPacket
import dev.slne.surf.cloud.standalone.persistent.StandalonePersistentData

class ServerInitializePacketListenerImpl(val connection: Connection) :
    ServerInitializePacketListener {

    override fun handleIdRequest(packet: ServerboundInitializeRequestIdPacket) {
        val id = StandalonePersistentData.SERVER_ID_COUNTER.value()
        StandalonePersistentData.SERVER_ID_COUNTER.setValue(id + 1)
        println("Generated id: $id")
        connection.send(ClientboundInitializeIdResponsePacket(id))
    }

    override fun onDisconnect(details: DisconnectionDetails) {
        // Do nothing
    }
}