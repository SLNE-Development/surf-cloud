package dev.slne.surf.cloud.api.common.util

import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.ApiStatus.NonExtendable

interface AdvancedAutoCloseable : AutoCloseable {
    @NonExtendable
    override fun close() = runBlocking { destroy() }

    suspend fun start()
    suspend fun destroy()
}
