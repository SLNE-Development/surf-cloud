package dev.slne.surf.cloud.api.server.plugin.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

interface CoroutineSession {
    val scope: CoroutineScope
    val dispatcher: CoroutineContext
}