package dev.slne.surf.cloud.api.common.util

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.netty.util.internal.TypeParameterMatcher

abstract class UnifiedReadOnlyChannelHandler<I> : ChannelDuplexHandler {
    private val matcher: TypeParameterMatcher

    constructor() {
        this.matcher =
            TypeParameterMatcher.find(this, UnifiedReadOnlyChannelHandler::class.java, "I")
    }

    constructor(messageType: Class<I>) {
        this.matcher = TypeParameterMatcher.get(messageType)
    }

    fun acceptMessage(msg: Any) = matcher.match(msg)

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (acceptMessage(msg)) {
            @Suppress("UNCHECKED_CAST")
            handleRead(ctx, msg as I)
        }

        super.channelRead(ctx, msg)
    }

    override fun write(
        ctx: ChannelHandlerContext,
        msg: Any,
        promise: ChannelPromise
    ) {
        if (acceptMessage(msg)) {
            @Suppress("UNCHECKED_CAST")
            handleWrite(ctx, msg as I, promise)
        }
        super.write(ctx, msg, promise)
    }

    protected abstract fun handleRead(ctx: ChannelHandlerContext, msg: I)
    protected abstract fun handleWrite(ctx: ChannelHandlerContext, msg: I, promise: ChannelPromise)
}