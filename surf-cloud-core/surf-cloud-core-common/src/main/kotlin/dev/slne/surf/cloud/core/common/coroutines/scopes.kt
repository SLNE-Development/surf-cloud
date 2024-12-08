package dev.slne.surf.cloud.core.common.coroutines

import dev.slne.surf.cloud.api.common.util.logger
import dev.slne.surf.cloud.api.common.util.threadFactory
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

object NettyListenerScope : CoroutineScope {
    private val executor = Executors.newCachedThreadPool(threadFactory {
        nameFormat("netty-listener-thread-%d")
        daemon(false)
    }).asCoroutineDispatcher()

    override val coroutineContext = executor + CoroutineName("netty-listener") + SupervisorJob()
}

object NettyConnectionScope : CoroutineScope {
    private val executor = Executors.newCachedThreadPool(threadFactory {
        nameFormat("netty-connection-thread-%d")
        daemon(false)
    }).asCoroutineDispatcher()

    override val coroutineContext = executor + CoroutineName("netty-connection") + SupervisorJob()
}

object NettyTickScope : CoroutineScope {
    private val log = logger()
    private val executor = Executors.newCachedThreadPool(threadFactory {
        nameFormat("netty-tick-thread-%d")
        daemon(false)
        uncaughtExceptionHandler { thread, throwable ->
            log.atSevere()
                .withCause(throwable)
                .log("Uncaught exception in tick thread %s", thread.name)
        }
    })

    override val coroutineContext =
        executor.asCoroutineDispatcher() + CoroutineName("netty-tick") + SupervisorJob()
}

object QueueScope : CoroutineScope {
    private val executor = Executors.newCachedThreadPool(threadFactory {
        nameFormat("queue-thread-%d")
        daemon(false)
    }).asCoroutineDispatcher()

    override val coroutineContext = executor + CoroutineName("queue") + SupervisorJob()
}

object QueueDisplayScope : CoroutineScope {
    private val executor = Executors.newSingleThreadExecutor(threadFactory {
        nameFormat("queue-display-thread-%d")
        daemon(false)
    }).asCoroutineDispatcher()

    override val coroutineContext = executor + CoroutineName("queue-display") + SupervisorJob()
}