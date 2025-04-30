package dev.slne.surf.cloud.core.common.coroutines

import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.api.common.util.threadFactory
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.*
import java.util.concurrent.Executors

abstract class BaseScope(
    dispatcher: CoroutineDispatcher,
    private val name: String
) : CoroutineScope {
    companion object {
        private val scopes = mutableObjectListOf<BaseScope>()
        fun terminateAll() {
            scopes.forEach { it.cancel("Shutdown") }
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

    val context get() = coroutineContext

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
    dispatcher = Dispatchers.IO,
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

object ConsoleCommandInputScope : BaseScope(
    dispatcher = Dispatchers.IO,
    name = "Server console handler"
)

object KtorScope : BaseScope(
    dispatcher = Dispatchers.IO,
    name = "ktor"
)

object NameHistoryScope : BaseScope(
    dispatcher = Dispatchers.IO,
    name = "name-history"
)

object PlayerDatabaseScope : BaseScope(
    dispatcher = newSingleThreadContext("player-database-thread"),
    name = "player-database"
)

object PlayerPlaytimeScope : BaseScope(
    dispatcher = Dispatchers.IO,
    name = "player-playtime"
)

object CloudServerCleanupScope : BaseScope(
    dispatcher = Dispatchers.Default,
    name = "cloud-server-cleanup"
)

object PunishmentHandlerScope : BaseScope(
    dispatcher = Dispatchers.Default,
    name = "common-punishment-handlers"
)

object PunishmentDatabaseScope : BaseScope(
    dispatcher = Dispatchers.IO,
    name = "punishment-database"
)

object PrePlayerJoinTaskScope : BaseScope(
    dispatcher = Dispatchers.IO,
    name = "pre-player-join-task"
)

object CloudEventBusScope : BaseScope(
    dispatcher = Dispatchers.Default,
    name = "cloud-event-bus"
)

object CloudConnectionVerificationScope : BaseScope(
    dispatcher = Dispatchers.Default,
    name = "cloud-connection-verification"
)