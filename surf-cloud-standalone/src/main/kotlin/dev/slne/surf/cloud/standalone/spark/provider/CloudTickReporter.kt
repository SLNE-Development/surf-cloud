package dev.slne.surf.cloud.standalone.spark.provider

import me.lucko.spark.common.tick.AbstractTickReporter

object CloudTickReporter : AbstractTickReporter() {
    private var started = false

    fun tick(duration: Double) {
        if (started) {
            onTick(duration)
        }
    }

    override fun start() {
        started = true
    }

    override fun close() {
        started = false
    }
}