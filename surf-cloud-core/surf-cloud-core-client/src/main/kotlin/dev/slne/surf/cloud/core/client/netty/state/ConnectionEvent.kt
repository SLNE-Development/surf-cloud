package dev.slne.surf.cloud.core.client.netty.state

data class ConnectionEvent(
    val from: StateMachine.State,
    val to: StateMachine.State,
    val cause: Throwable? = null
)