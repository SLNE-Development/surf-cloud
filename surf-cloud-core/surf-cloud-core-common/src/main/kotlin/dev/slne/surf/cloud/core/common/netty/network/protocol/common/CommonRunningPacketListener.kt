package dev.slne.surf.cloud.core.common.netty.network.protocol.common

import dev.slne.surf.cloud.core.common.netty.network.PacketListener
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.*

interface CommonRunningPacketListener: PacketListener {

    fun handlePlayerDisconnectFromServer(packet: PlayerDisconnectFromServerPacket)

    fun handleRequestOfflinePlayerDisplayName(packet: RequestOfflineDisplayNamePacket)

    fun handleRequestLuckpermsMetaData(packet: RequestLuckpermsMetaDataPacket)

    fun handleDisconnectPlayer(packet: DisconnectPlayerPacket)

    fun handleSilentDisconnectPlayer(packet: SilentDisconnectPlayerPacket)

    fun handleTeleportPlayer(packet: TeleportPlayerPacket)

    fun handleTeleportPlayerToPlayer(packet: TeleportPlayerToPlayerPacket)

    fun handleUpdateAFKState(packet: UpdateAFKStatePacket)

    fun handleRequestPlayerPermission(packet: RequestPlayerPermissionPacket)

    fun handleSendToast(packet: SendToastPacket)

    fun handleUpdatePlayerPersistentDataContainer(packet: UpdatePlayerPersistentDataContainerPacket)


}