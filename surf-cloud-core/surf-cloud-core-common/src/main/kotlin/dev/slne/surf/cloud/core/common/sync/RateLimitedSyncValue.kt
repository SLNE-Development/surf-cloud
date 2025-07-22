package dev.slne.surf.cloud.core.common.sync

import dev.slne.surf.cloud.api.common.sync.SyncValue
import dev.slne.surf.cloud.core.common.coroutines.SyncValueScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlin.time.Duration

@OptIn(FlowPreview::class)
class RateLimitedSyncValue<T> internal constructor(
    private val delegate: BasicSyncValue<T>,
    minDuration: Duration
) : SyncValue<T> by delegate {
    private val updates = MutableStateFlow(delegate.get())

    init {
        SyncValueScope.launch {
            updates
                .debounce(minDuration)
                .collect { value ->
                    delegate.set(value)
                }
        }
    }

    override fun set(newValue: T) {
        updates.value = newValue
    }
}