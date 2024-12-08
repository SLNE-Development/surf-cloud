package dev.slne.surf.cloud.core.common.coroutines

import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.api.common.util.threadFactory
import kotlinx.coroutines.*
import java.util.concurrent.Executors

abstract class BaseScope(
    dispatcher: CoroutineDispatcher,
    private val name: String
) : CoroutineScope {
    protected val log = logger()
    override val coroutineContext = dispatcher + CoroutineName(name) + SupervisorJob() +
            CoroutineExceptionHandler { context, throwable ->
                val coroutineName = context[CoroutineName]?.name ?: "Unnamed Coroutine"
                log.atSevere()
                    .withCause(throwable)
                    .log("Unhandled exception in coroutine: $coroutineName")
            }
}

object PacketHandlerScope: BaseScope(
    dispatcher = Dispatchers.Default,
    name = "netty-listener"
)

object ConnectionManagementScope : BaseScope(
    dispatcher = Dispatchers.Default,
    name = "netty-connection"
)

object ConnectionTickScope : BaseScope(
    dispatcher = Executors.newSingleThreadExecutor(threadFactory {
        nameFormat("netty-tick-thread-%d")
        daemon(false)
        uncaughtExceptionHandler { thread, throwable ->
            logger().atSevere()
                .withCause(throwable)
                .log("Uncaught exception in tick thread %s", thread.name)
        }
    }).asCoroutineDispatcher(),
    name = "netty-tick"
)

object QueueProcessingScope : BaseScope(
    dispatcher = Executors.newSingleThreadExecutor(threadFactory {
        nameFormat("queue-thread-%d")
        daemon(false)
    }).asCoroutineDispatcher(),
    name = "queue"
)

object QueueDisplayScope : BaseScope(
    dispatcher = Executors.newSingleThreadExecutor(threadFactory {
        nameFormat("queue-display-thread-%d")
        daemon(false)
    }).asCoroutineDispatcher(),
    name = "queue-display"
)