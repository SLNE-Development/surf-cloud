package dev.slne.surf.cloud.api.common.util

import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.springframework.util.StopWatch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class TimeLogger(private val label: String) {
    @PublishedApi
    internal val logger = ComponentLogger.logger(label)

    @PublishedApi
    internal val stopWatch = StopWatch(label)

    @PublishedApi
    internal val taskCounter = AtomicInteger()

    @PublishedApi
    internal val taskNames = ConcurrentHashMap<Int, String>()

    inline fun <T> measureStep(name: String, block: () -> T): T {
        val index = taskCounter.incrementAndGet()
        taskNames[index] = name
        logger.info("▶️  Step $index: $name started...")
        stopWatch.start("Step $index: $name")
        val result = block()
        stopWatch.stop()
        logger.info("✅ Step $index: $name completed in %.2fs".format(stopWatch.lastTaskInfo().timeSeconds))
        return result
    }

    fun printSummary() {
        logger.info("⏱️  Total Time: %.2fs".format(stopWatch.totalTimeSeconds))
        logger.info(stopWatch.prettyPrint())
    }

    fun totalSeconds(): Double = stopWatch.totalTimeSeconds
}