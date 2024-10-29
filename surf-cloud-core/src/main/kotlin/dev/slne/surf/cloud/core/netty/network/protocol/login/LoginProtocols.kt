package dev.slne.surf.cloud.core.netty.network.protocol.login

import dev.slne.surf.cloud.api.netty.network.ConnectionProtocol
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf
import dev.slne.surf.cloud.core.netty.network.protocol.ProtocolInfoBuilder
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.login.clientbound.ClientboundInitializeClientPacket
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.login.clientbound.ClientboundLoginDisconnectPacket
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.login.clientbound.ClientboundLoginFinishedPacket
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.login.serverbound.ServerboundLoginAcknowledgedPacket
import dev.slne.surf.cloud.core.netty.protocol.packets.cloud.phase.login.serverbound.ServerboundLoginStartPacket

object LoginProtocols {

    val SERVERBOUND_TEMPLATE =
        ProtocolInfoBuilder.serverboundProtocol<ServerLoginPacketListener, SurfByteBuf>(
            ConnectionProtocol.LOGIN
        ) { builder ->
            builder.addPacket(ServerboundLoginStartPacket.STREAM_CODEC)
            builder.addPacket(ServerboundLoginAcknowledgedPacket.STREAM_CODEC)
        }

    val SERVERBOUND = SERVERBOUND_TEMPLATE.bind(::SurfByteBuf)

    val CLIENTBOUND_TEMPLATE =
        ProtocolInfoBuilder.clientboundProtocol<ClientLoginPacketListener, SurfByteBuf>(
            ConnectionProtocol.LOGIN
        ) { builder ->
            builder.addPacket(ClientboundLoginFinishedPacket.STREAM_CODEC)
            builder.addPacket(ClientboundInitializeClientPacket.STREAM_CODEC)
            builder.addPacket(ClientboundLoginDisconnectPacket.STREAM_CODEC)
        }

    val CLIENTBOUND = CLIENTBOUND_TEMPLATE.bind(::SurfByteBuf)
}