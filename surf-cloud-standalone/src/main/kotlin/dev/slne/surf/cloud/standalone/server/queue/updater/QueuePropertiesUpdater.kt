package dev.slne.surf.cloud.standalone.server.queue.updater

import dev.slne.surf.cloud.api.common.event.CloudEventHandler
import dev.slne.surf.cloud.standalone.event.server.CloudServerRegisteredEvent
import dev.slne.surf.cloud.standalone.server.StandaloneCloudServerImpl
import dev.slne.surf.cloud.standalone.server.queue.repo.QueueRepository
import org.springframework.stereotype.Component

@Suppress("unused")
@Component
class QueuePropertiesUpdater(private val queues: QueueRepository) {

    @CloudEventHandler
    fun onServerRegistered(event: CloudServerRegisteredEvent) {
        val server = event.server
        if (server !is StandaloneCloudServerImpl) return

        val queue = queues.getServerOrNull(server.uid) ?: return
        queue.latestGroup = server.group
    }
}