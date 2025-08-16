package dev.slne.surf.cloud.core.common.player.cache

import java.util.concurrent.atomic.AtomicLong

class ChangeCounter {
    private val current = AtomicLong(0)
    fun next() = current.incrementAndGet()
    fun current() = current.get()
}