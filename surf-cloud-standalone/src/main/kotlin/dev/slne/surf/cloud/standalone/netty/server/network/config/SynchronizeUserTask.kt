package dev.slne.surf.cloud.standalone.netty.server.network.config

import com.github.benmanes.caffeine.cache.Caffeine
import com.google.common.flogger.StackSize
import dev.slne.surf.cloud.api.common.netty.NettyClient
import dev.slne.surf.cloud.api.common.plugin.spring.task.CloudInitialSynchronizeTask
import dev.slne.surf.cloud.api.common.util.Either
import dev.slne.surf.cloud.core.common.netty.network.MAX_PACKET_SIZE
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.*
import dev.slne.surf.cloud.core.common.netty.network.protocol.synchronizing.ClientboundSyncPlayerHydrationChunkPacket.Entry
import dev.slne.surf.cloud.standalone.player.StandaloneCloudPlayerImpl
import dev.slne.surf.cloud.standalone.player.standalonePlayerManagerImpl
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import dev.slne.surf.surfapi.core.api.util.toMutableObjectList
import io.netty.buffer.Unpooled
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.nbt.CompoundBinaryTag
import java.util.*
import java.util.concurrent.TimeUnit

object SynchronizeUserTask : CloudInitialSynchronizeTask {
    private const val MAX_PDC_SIZE = MAX_PACKET_SIZE - 0xF4240 // ~7 MB for pdc
    private const val MAX_HYDRATION_DATA_SIZE = MAX_PACKET_SIZE - 0x186A0
    private val log = logger()

    private val pendingPdcs = Caffeine.newBuilder()
        .expireAfterWrite(2, TimeUnit.MINUTES)
        .removalListener<String, ObjectSet<UUID>> { key, value, cause ->
            if (value != null && value.isNotEmpty()) {
                log.atWarning()
                    .withStackTrace(StackSize.SMALL)
                    .log(
                        "Unable to transfer all player PDCs to server '%s'. Pending PDC callbacks: %s. Cause: %s",
                        key,
                        value,
                        cause
                    )
            }
        }
        .build<String, ObjectSet<UUID>>()

    override suspend fun execute(client: NettyClient) {
        val players = standalonePlayerManagerImpl.getRawOnlinePlayers()

        syncPlayerHydration(client, players)
        syncPendingPersistentDataContainers(client, players)
    }

    private fun syncPlayerHydration(
        client: NettyClient,
        players: List<StandaloneCloudPlayerImpl>
    ) {
        val tempBuf = Unpooled.directBuffer(1024)
        try {
            client.fireAndForget(ClientboundSyncPlayerHydrationStartPacket)

            val current = mutableObjectListOf<Entry>()
            var currentSize = 0

            fun estimatePlayerSize(entry: Entry): Int {
                tempBuf.clear()
                Entry.STREAM_CODEC.encode(tempBuf, entry)
                return tempBuf.readableBytes()
            }

            for (player in players) {
                val data = player.toHydrationData(client)
                val playerSize = estimatePlayerSize(data)

                if (currentSize + playerSize > MAX_HYDRATION_DATA_SIZE && current.isNotEmpty()) {
                    client.fireAndForget(ClientboundSyncPlayerHydrationChunkPacket(current.toMutableObjectList()))
                    current.clear()
                    currentSize = 0
                }

                current.add(data)
                currentSize += playerSize
            }

            if (current.isNotEmpty()) {
                client.fireAndForget(ClientboundSyncPlayerHydrationChunkPacket(current.toMutableObjectList()))
            }

            current.clear()
            tempBuf.clear()

            client.fireAndForget(ClientboundSyncPlayerHydrationEndPacket)
        } finally {
            tempBuf.release()
        }
    }

    private fun syncPendingPersistentDataContainers(
        client: NettyClient,
        players: List<StandaloneCloudPlayerImpl>
    ) {
        val pendingPdcs = pendingPdcs.getIfPresent(client.serverName)
        if (pendingPdcs.isNullOrEmpty()) return
        val iterator = pendingPdcs.iterator()
        while (iterator.hasNext()) {
            val uuid = iterator.next()
            val player = players.find { it.uuid == uuid } ?: run {
                log.atWarning()
                    .log(
                        "Unable to find player with UUID '%s' in hydration list. Maybe the player already disconnected?",
                        uuid
                    )
                continue
            }

            client.fireAndForget(
                ClientboundSyncLargePlayerPersistentDataContainerStartPacket(
                    uuid
                )
            )

            val data = player.ppdcToByteArray()
            var offset = 0
            while (offset < data.size) {
                val chunkSize = minOf(MAX_PDC_SIZE, data.size - offset)
                val chunk = data.copyOfRange(offset, offset + chunkSize)
                offset += chunkSize

                client.fireAndForget(
                    ClientboundSyncLargePlayerPersistentDataContainerChunkPacket(
                        chunk
                    )
                )
            }

            client.fireAndForget(ClientboundSyncLargePlayerPersistentDataContainerEndPacket)
            iterator.remove()
        }
    }

    private fun StandaloneCloudPlayerImpl.toHydrationData(client: NettyClient): Entry {
        val ppdcSize = estimatedPpdcSize()
        val fits = MAX_PDC_SIZE >= ppdcSize
        val pdcOrCallback: Either<CompoundBinaryTag, UUID> = if (fits) {
            Either.left(persistentData.toTagCompound())
        } else {
            pendingPdcs.get(client.serverName) { mutableObjectSetOf() }.add(uuid)
            Either.right(uuid)
        }

        return Entry(
            uuid,
            name,
            server?.name,
            proxyServer?.name,
            ip,
            pdcOrCallback
        )
    }
}