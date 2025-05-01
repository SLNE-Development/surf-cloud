package dev.slne.surf.cloud.api.common.event

abstract class CancellableCloudEvent(source: Any) : CloudEvent(source), Cancellable {
    @Volatile
    override var isCancelled: Boolean = false
}