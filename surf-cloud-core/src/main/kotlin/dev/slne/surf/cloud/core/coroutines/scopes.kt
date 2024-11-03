package dev.slne.surf.cloud.core.coroutines

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

object NettyListenerScope : CoroutineScope {
    private val executor = Executors.newCachedThreadPool(object : ThreadFactory {
        val factory = BasicThreadFactory.Builder()
            .namingPattern("netty-listener-thread-%d")
            .daemon(false)
            .build()

        override fun newThread(r: Runnable): Thread = factory.newThread(r)

    }).asCoroutineDispatcher()

    override val coroutineContext = executor + CoroutineName("netty-listener") + SupervisorJob()
}

object NettyConnectionScope : CoroutineScope {
    private val executor = Executors.newCachedThreadPool(object : ThreadFactory {
        val factory = BasicThreadFactory.Builder()
            .namingPattern("netty-connection-thread-%d")
            .daemon(false)
            .build()

        override fun newThread(r: Runnable): Thread = factory.newThread(r)

    }).asCoroutineDispatcher()

    override val coroutineContext = executor + CoroutineName("netty-connection") + SupervisorJob()
}