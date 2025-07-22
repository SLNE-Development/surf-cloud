package dev.slne.surf.cloud.standalone.spark.provider

import me.lucko.spark.common.tick.AbstractTickHook

object CloudTickHook: AbstractTickHook() {
    private var started = false

    fun tick() {
        if (started) {
            onTick()
        }
    }

    override fun start() {
        started = true
    }

    override fun close() {
        started = false
    }
}