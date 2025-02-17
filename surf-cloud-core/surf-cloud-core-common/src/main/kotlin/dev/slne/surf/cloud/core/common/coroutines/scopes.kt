package dev.slne.surf.cloud.core.common.coroutines

import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.api.common.util.threadFactory
import kotlinx.coroutines.*
import java.util.concurrent.Executors

abstract class BaseScope(
    dispatcher: CoroutineDispatcher,
    private val name: String
) : CoroutineScope {
    companion object {
        private val scopes = mutableObjectListOf<BaseScope>()
        fun terminateAll() {
            scopes.forEach { it.cancel() }
        }
    }

    protected val log = logger()

    init {
        scopes.add(this)
    }

    override val coroutineContext = dispatcher + CoroutineName(name) + SupervisorJob() +
            CoroutineExceptionHandler { context, throwable ->
                val coroutineName = context[CoroutineName]?.name ?: "Unnamed Coroutine"
                log.atSevere()
                    .withCause(throwable)
                    .log("Unhandled exception in coroutine: $coroutineName")
            }
}

object PacketHandlerScope : BaseScope(
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

object PlayerDataSaveScope : BaseScope(
    dispatcher = Executors.newSingleThreadExecutor(threadFactory {
        nameFormat("player-data-save-thread-%d")
        daemon(false)
    }).asCoroutineDispatcher(),
    name = "player-data-save"
)

object PlayerBatchTransferScope : BaseScope(
    dispatcher = Executors.newSingleThreadExecutor(threadFactory {
        nameFormat("player-batch-transfer-thread-%d")
        daemon(false)
    }).asCoroutineDispatcher(),
    name = "player-batch-transfer"
)

object ServerShutdownScope : BaseScope(
    dispatcher = Executors.newSingleThreadExecutor(threadFactory {
        nameFormat("server-shutdown-thread-%d")
        daemon(false)
    }).asCoroutineDispatcher(),
    name = "server-shutdown"
)

object ConsoleCommandHandlerScope : BaseScope(
    dispatcher = Dispatchers.IO,
    name = "console-command-handler"
)

object ConsoleCommandInputScope : BaseScope(
    dispatcher = Dispatchers.IO,
    name = "Server console handler"
)

object KtorScope : BaseScope(
    dispatcher = Dispatchers.IO,
    name = "ktor"
)