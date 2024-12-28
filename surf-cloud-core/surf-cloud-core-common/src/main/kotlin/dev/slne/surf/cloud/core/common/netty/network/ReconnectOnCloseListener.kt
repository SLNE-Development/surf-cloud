package dev.slne.surf.cloud.core.common.netty.network

import dev.slne.surf.cloud.api.common.netty.NettyClient
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelFutureListener
import kotlinx.coroutines.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

val defaultReconnectInterval = 5.seconds

class ReconnectOnCloseListener(
    private val client: NettyClient,
    private val reconnectInterval: Duration = defaultReconnectInterval,
) : ChannelFutureListener {

    override fun operationComplete(channelFuture: ChannelFuture) {
        val channel = channelFuture.channel()

        channel.disconnect()

        GlobalScope.launch { // TODO: Fixme
            scheduleReconnect()
        }
    }

    private suspend fun scheduleReconnect() = withContext(Dispatchers.IO) {
        delay(reconnectInterval)

        (client.connection as ConnectionImpl)
            .reconnect(DisconnectionDetails("Reconnecting..."))
    }

}