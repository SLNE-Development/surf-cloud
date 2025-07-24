package dev.slne.surf.cloud.standalone.netty.server.network

import dev.slne.surf.cloud.core.common.netty.network.ConnectionImpl
import dev.slne.surf.cloud.core.common.netty.network.DisconnectionDetails
import dev.slne.surf.cloud.core.common.netty.network.protocol.initialize.ServerInitializePacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.initialize.clientbound.ClientboundInitializeIdResponsePacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.initialize.serverbound.ServerboundInitializeRequestIdPacket
import dev.slne.surf.cloud.standalone.persistent.StandalonePersistentData

class ServerInitializePacketListenerImpl(val connection: ConnectionImpl) :
    ServerInitializePacketListener {

    override fun handleIdRequest(packet: ServerboundInitializeRequestIdPacket) {
        val id = StandalonePersistentData.SERVER_ID_COUNTER.value()
        StandalonePersistentData.SERVER_ID_COUNTER.setValue(id + 1)
        connection.send(ClientboundInitializeIdResponsePacket(id))
    }

    override suspend fun onDisconnect(details: DisconnectionDetails) {
        // Do nothing
    }

    override fun isAcceptingMessages(): Boolean {
        return connection.connected
    }
}