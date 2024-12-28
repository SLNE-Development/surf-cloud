package dev.slne.surf.cloud.velocity.netty.listener

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PreLoginEvent
import dev.slne.surf.cloud.core.common.messages.MessageManager

object NettyPlayerConnectionBlocker {

    @Subscribe(order = PostOrder.FIRST)
    fun onPreLogin(event: PreLoginEvent) {
        event.result = PreLoginEvent.PreLoginComponentResult.denied(MessageManager.serverStarting)
    }
}