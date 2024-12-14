package dev.slne.surf.cloud.core.common.netty

import dev.slne.surf.cloud.api.common.util.logger
import org.jetbrains.annotations.MustBeInvokedByOverriders

abstract class NettyManager {

    private val log = logger()

    @MustBeInvokedByOverriders
    open suspend fun bootstrap() {
        log.atInfo().log("Bootstrapping NettyManager...")
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