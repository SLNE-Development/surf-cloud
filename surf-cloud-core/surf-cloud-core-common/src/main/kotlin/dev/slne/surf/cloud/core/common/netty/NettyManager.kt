package dev.slne.surf.cloud.core.common.netty

abstract class NettyManager {

    open fun blockPlayerConnections() {
    }

    open fun unblockPlayerConnections() {
    }

    open suspend fun afterStart() {
    }

    open fun stop() {

    }
}