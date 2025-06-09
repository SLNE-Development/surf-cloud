package dev.slne.surf.cloud.standalone.spark.provider

import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.cloud.api.common.server.CommonCloudServer
import dev.slne.surf.cloud.api.server.server.ServerCloudServer
import dev.slne.surf.cloud.api.server.server.ServerProxyCloudServer
import dev.slne.surf.cloud.core.common.data.PersistentDataImpl
import dev.slne.surf.cloud.standalone.server.serverManagerImpl
import me.lucko.spark.common.platform.MetadataProvider
import me.lucko.spark.lib.gson.JsonObject
import me.lucko.spark.lib.gson.JsonParser

object CloudMetadataProvider : MetadataProvider {
    override fun get() = mapOf(
        "onlinePlayers" to collectOnlinePlayers(),
        "serverInfos" to collectServerInfos(),
        "persistentData" to collectPersistentData()
    )

    fun collectPersistentData() = JsonObject().apply {
        PersistentDataImpl.tag.forEach { (key, tag) ->
            add(key, JsonParser.parseString(tag.valueToString()))
        }
    }

    fun collectOnlinePlayers() = JsonObject().apply {
        CloudPlayerManager.getOnlinePlayers().forEach { player ->
            add(player.uuid.toString(), JsonObject().apply {
                addProperty("name", player.name)
                addProperty("isAfk", player.isAfk())
                add("server", player.currentServer().toJsonObject())
            })
        }
    }

    fun collectServerInfos() = JsonObject().apply {
        serverManagerImpl.getAllServersUnsafe().forEach { server ->
            add(server.name, server.toJsonObject(true))
        }
    }

    fun CommonCloudServer.toJsonObject(fullInfo: Boolean = false): JsonObject {
        return JsonObject().apply {
            addProperty("uid", uid)
            addProperty("group", group)
            addProperty("name", name)
            if (!fullInfo) return@apply

            addProperty("maxPlayerCount", maxPlayerCount)
            addProperty("currentPlayerCount", currentPlayerCount)
            addProperty("isProxy", this@toJsonObject is ServerProxyCloudServer)

            if (this@toJsonObject is ServerCloudServer) {
                addProperty("hasAllowList", allowlist)
            }
        }
    }
}