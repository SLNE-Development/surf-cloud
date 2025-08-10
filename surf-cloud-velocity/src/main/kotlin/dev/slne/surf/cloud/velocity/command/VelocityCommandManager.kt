package dev.slne.surf.cloud.velocity.command

import dev.slne.surf.cloud.api.common.util.TimeLogger
import dev.slne.surf.cloud.core.common.spring.CloudLifecycleAware
import dev.slne.surf.cloud.velocity.command.cloud.cloudCommand
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(CloudLifecycleAware.MISC_PRIORITY)
class VelocityCommandManager : CloudLifecycleAware {

    override suspend fun onEnable(timeLogger: TimeLogger) {
        timeLogger.measureStep("Register Cloud commands") {
            registerCommands()
        }
    }

    private fun registerCommands() {
        cloudCommand()
    }
}