@file:OptIn(InternalApi::class)

package dev.slne.surf.cloud.api.client.netty.packet

import dev.slne.surf.cloud.api.client.netty.CloudClientNettyManager
import dev.slne.surf.cloud.api.common.netty.network.protocol.CommonResponseType
import dev.slne.surf.cloud.api.common.netty.packet.DEFAULT_TIMEOUT
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import kotlin.time.Duration

suspend fun NettyPacket.fire(convertExceptions: Boolean = true) {
    CloudClientNettyManager.client.fire(this, convertExceptions)
}

fun NettyPacket.fireAndForget() {
    CloudClientNettyManager.client.fireAndForget(this)
}

suspend fun <P : ResponseNettyPacket> RespondingNettyPacket<P>.fireAndAwait(timeout: Duration = DEFAULT_TIMEOUT): P? =
    fireAndAwait(CloudClientNettyManager.client.connection, timeout)

suspend fun <P : ResponseNettyPacket> RespondingNettyPacket<P>.fireAndAwaitUrgent(): P? =
    fireAndAwaitUrgent(CloudClientNettyManager.client.connection)

suspend fun <P : ResponseNettyPacket> RespondingNettyPacket<P>.fireAndAwaitOrThrow(timeout: Duration = DEFAULT_TIMEOUT): P =
    fireAndAwaitOrThrow(CloudClientNettyManager.client.connection, timeout)

suspend fun <P : ResponseNettyPacket> RespondingNettyPacket<P>.fireAndAwaitOrThrowUrgent(): P =
    fireAndAwaitOrThrowUrgent(CloudClientNettyManager.client.connection)


suspend inline fun <reified T, R> RespondingNettyPacket<T>.await(timeout: Duration = DEFAULT_TIMEOUT): R? where T : ResponseNettyPacket, T : CommonResponseType<R> {
    return fireAndAwait(timeout)?.value
}

suspend inline fun <reified T, R> RespondingNettyPacket<T>.awaitUrgent(): R? where T : ResponseNettyPacket, T : CommonResponseType<R> {
    return fireAndAwaitUrgent()?.value
}

suspend inline fun <reified T, R> RespondingNettyPacket<T>.awaitOrThrow(timeout: Duration = DEFAULT_TIMEOUT): R where T : ResponseNettyPacket, T : CommonResponseType<R> {
    return fireAndAwaitOrThrow(timeout).value
}

suspend inline fun <reified T, R> RespondingNettyPacket<T>.awaitOrThrowUrgent(): R where T : ResponseNettyPacket, T : CommonResponseType<R> {
    return fireAndAwaitOrThrowUrgent().value
}