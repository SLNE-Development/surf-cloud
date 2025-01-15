package dev.slne.surf.cloud.standalone.plugin.entrypoint.classloader

import dev.slne.surf.cloud.api.server.plugin.configuration.PluginMeta

interface ClassloaderBytecodeModifier {
    companion object {
        val instance: ClassloaderBytecodeModifier = NoopClassloaderBytecodeModifier
    }

    fun modify(meta: PluginMeta, bytes: ByteArray): ByteArray
}

object NoopClassloaderBytecodeModifier : ClassloaderBytecodeModifier {
    override fun modify(
        meta: PluginMeta,
        bytes: ByteArray
    ): ByteArray {
        return bytes
    }
}