package dev.slne.surf.cloud.api.server.plugin.configuration

interface PluginMeta {
    val name: String
    val flywayTableName: String

    val version: String
    val main: String

    val pluginDependencies: Set<String>
    val pluginSoftDependencies: Set<String>
    val loadBefore: Set<String>

    val authors: Set<String>
    val contributors: Set<String>

    val description: String?

    val displayName: String
        get() = "$name v$version"

}