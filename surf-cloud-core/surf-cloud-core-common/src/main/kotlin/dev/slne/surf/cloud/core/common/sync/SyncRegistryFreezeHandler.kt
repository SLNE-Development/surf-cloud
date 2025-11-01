package dev.slne.surf.cloud.core.common.sync

import dev.slne.surf.cloud.api.common.util.TimeLogger
import dev.slne.surf.cloud.core.common.spring.CloudLifecycleAware
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(CloudLifecycleAware.BEFORE_START_TASK_PRIORITY)
class SyncRegistryFreezeHandler : CloudLifecycleAware {
    override suspend fun onEnable(timeLogger: TimeLogger) {
        timeLogger.measureStep("Freezing SyncRegistry") {
            CommonSyncRegistryImpl.instance.freeze()
        }
    }
}