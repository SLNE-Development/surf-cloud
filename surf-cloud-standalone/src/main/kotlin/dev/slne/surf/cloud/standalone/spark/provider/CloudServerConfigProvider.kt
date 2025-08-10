package dev.slne.surf.cloud.standalone.spark.provider

import me.lucko.spark.common.platform.serverconfig.ConfigParser
import me.lucko.spark.common.platform.serverconfig.ExcludedConfigFilter
import me.lucko.spark.common.platform.serverconfig.ServerConfigProvider
import me.lucko.spark.lib.gson.Gson
import me.lucko.spark.lib.gson.JsonElement
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.BufferedReader
import java.io.IOException
import kotlin.io.path.Path


private object YamlConfigParser : ConfigParser {
    private val GSON = Gson()

    override fun load(file: String, filter: ExcludedConfigFilter): JsonElement? {
        val values = parse(Path(file)) ?: return null
        return filter.apply(GSON.toJsonTree(values))
    }

    @Throws(IOException::class)
    override fun parse(reader: BufferedReader): Map<String, Any?> {
        val config = YamlConfigurationLoader.builder()
            .source { reader }
            .build()
            .load()

        return configToMap(config)
    }

    fun configToMap(node: ConfigurationNode): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        for ((key, child) in node.childrenMap()) {
            val value = when {
                child.isMap -> configToMap(child)
                child.isList -> child.childrenList().map { listNode ->
                    if (listNode.isMap) configToMap(listNode)
                    else listNode.raw()
                }

                else -> child.raw()
            }

            result[key.toString()] = value
        }

        return result
    }
}

object CloudServerConfigProvider : ServerConfigProvider(
    mapOf(
        "standalone-config.yml" to YamlConfigParser,
        "config.yml" to YamlConfigParser,
    ),
    listOf(
        "ktor.bearer-token",
        "connection.database.password",
        "connection.redis.password",
        "proxy.secret.manual-secret"
    )
)