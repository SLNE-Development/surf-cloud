package dev.slne.surf.cloud.api.server.queue

import org.jetbrains.annotations.ApiStatus

@ApiStatus.NonExtendable
interface GroupQueue : BaseQueue<GroupQueue> {
    val group: String
}