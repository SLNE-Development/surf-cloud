package dev.slne.surf.cloud.core.client.netty

import dev.slne.surf.cloud.api.common.util.TimeLogger
import dev.slne.surf.cloud.core.common.CloudCoreInstance
import dev.slne.surf.cloud.core.common.coroutines.CloudConnectionVerificationScope
import dev.slne.surf.cloud.core.common.spring.CloudLifecycleAware
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.minutes

@Component
class ConnectionVerifier : CloudLifecycleAware {
    override suspend fun onBootstrap(
        data: CloudCoreInstance.BootstrapData,
        timeLogger: TimeLogger
    ) {
        CloudConnectionVerificationScope.launch {
            while (isActive) {
                delay(5.minutes)
            }
        }
    }
}