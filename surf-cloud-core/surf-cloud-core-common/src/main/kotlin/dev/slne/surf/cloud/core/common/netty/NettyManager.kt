package dev.slne.surf.cloud.core.common.netty

import dev.slne.surf.surfapi.core.api.util.logger
import org.jetbrains.annotations.MustBeInvokedByOverriders

lateinit var nettyManager: NettyManager
abstract class NettyManager {

    private val log = logger()

    init {
        nettyManager = this
    }

    @MustBeInvokedByOverriders
    open suspend fun bootstrap() {
    }

    @MustBeInvokedByOverriders
    open suspend fun onLoad() {
        log.atInfo().log("Loading NettyManager...")
    }

    @MustBeInvokedByOverriders
    open suspend fun onEnable() {
        log.atInfo().log("Enabling NettyManager...")
    }

    @MustBeInvokedByOverriders
    open suspend fun afterStart() {
        log.atInfo().log("NettyManager started.")
    }

    @MustBeInvokedByOverriders
    open fun stop() {
        log.atInfo().log("Stopping NettyManager...")
        for (thread in Thread.getAllStackTraces().keys) {
            println("${thread.name} - ${thread.state}")
        }
    }

    @MustBeInvokedByOverriders
    open fun blockPlayerConnections() {
        log.atInfo().log("Blocking player connections...")
    }

    @MustBeInvokedByOverriders
    open fun unblockPlayerConnections() {
        log.atInfo().log("Unblocking player connections...")
    }
}