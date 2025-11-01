package dev.slne.surf.cloud.standalone.plugin.entrypoint.strategy

import java.io.Serial

class PluginGraphCycleException(val cycles: List<List<String>>) : RuntimeException() {
    companion object {
        @Serial
        val serialVersionUID: Long = 1L
    }
}