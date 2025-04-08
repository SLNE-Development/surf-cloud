package dev.slne.surf.cloud.core.client.player

import dev.slne.surf.cloud.api.client.netty.packet.fireAndAwaitOrThrow
import dev.slne.surf.cloud.api.common.player.name.NameHistory
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundRequestPlayerDataPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundRequestPlayerDataPacket.DataRequestType
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.getGenericValue
import dev.slne.surf.cloud.core.common.player.CommonOfflineCloudPlayerImpl
import net.kyori.adventure.text.Component
import java.net.Inet4Address
import java.time.ZonedDateTime
import java.util.*

class OfflineCloudPlayerImpl(uuid: UUID) : CommonOfflineCloudPlayerImpl(uuid) {
    override suspend fun nameHistory(): NameHistory {
        return request(DataRequestType.NAME_HISTORY)
    }

    override suspend fun lastServerRaw(): String? {
        return request(DataRequestType.LAST_SERVER)
    }

    override suspend fun lastSeen(): ZonedDateTime? {
        return request(DataRequestType.LAST_SEEN)
    }

    override suspend fun latestIpAddress(): Inet4Address? {
        return request(DataRequestType.LATEST_IP_ADDRESS)
    }

    override suspend fun displayName(): Component? {
        return request(DataRequestType.DISPLAY_NAME)
    }

    override suspend fun name(): String? {
        return request(DataRequestType.NAME)
    }

    override suspend fun <R> getLuckpermsMetaData(
        key: String,
        transformer: (String) -> R
    ): R? {
        TODO("Not yet implemented")
    }

    override suspend fun getLuckpermsMetaData(key: String): String? {
        TODO("Not yet implemented")
    }

    private suspend inline fun <reified T> request(type: DataRequestType): T {
        val data = ServerboundRequestPlayerDataPacket(uuid, type).fireAndAwaitOrThrow()
        return data.data.getGenericValue<T>()
    }
}