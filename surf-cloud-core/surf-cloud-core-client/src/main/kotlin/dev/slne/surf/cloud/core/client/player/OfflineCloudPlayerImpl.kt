package dev.slne.surf.cloud.core.client.player

import dev.slne.surf.cloud.api.client.netty.packet.fireAndAwaitOrThrow
import dev.slne.surf.cloud.api.common.player.name.NameHistory
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundRequestOfflinePlayerData
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundRequestOfflinePlayerData.RequestType
import dev.slne.surf.cloud.core.common.player.CommonOfflineCloudPlayerImpl
import net.kyori.adventure.text.Component
import java.net.InetAddress
import java.time.ZonedDateTime
import java.util.*

class OfflineCloudPlayerImpl(uuid: UUID) : CommonOfflineCloudPlayerImpl(uuid) {
    override suspend fun nameHistory(): NameHistory {
        TODO("Not yet implemented")
    }

    override suspend fun lastServerRaw(): String? {
        return request(RequestType.LAST_SERVER)
    }

    override suspend fun lastSeen(): ZonedDateTime? {
        return request(RequestType.LAST_SEEN)
    }

    override suspend fun latestIpAddress(): InetAddress? {
        return request(RequestType.LATEST_IP_ADDRESS)
    }

    override suspend fun displayName(): Component? {
        return request(RequestType.DISPLAY_NAME)
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

    private suspend fun <T> request(type: RequestType<T>): T {
        return ServerboundRequestOfflinePlayerData(uuid, type).fireAndAwaitOrThrow().value
    }
}