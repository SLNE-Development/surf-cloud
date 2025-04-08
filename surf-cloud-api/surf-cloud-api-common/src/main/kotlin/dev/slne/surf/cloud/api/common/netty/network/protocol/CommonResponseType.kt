package dev.slne.surf.cloud.api.common.netty.network.protocol

import dev.slne.surf.cloud.api.common.netty.network.Connection
import dev.slne.surf.cloud.api.common.netty.packet.DEFAULT_TIMEOUT
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.util.annotation.InternalApi
import kotlin.reflect.full.companionObjectInstance
import kotlin.time.Duration

@InternalApi
interface CommonResponseType<T> {
    val value: T
}

@InternalApi
@PublishedApi
internal interface CommonResponseTypeFactory<T : ResponseNettyPacket, R> {
    fun create(value: R): T
}

inline fun <reified T, R> RespondingNettyPacket<T>.respond(value: R) where T : ResponseNettyPacket, T : CommonResponseType<R> {
    respond(extractFactory<T, R>().create(value))
}

suspend inline fun <reified T, R> RespondingNettyPacket<T>.await(
    connection: Connection,
    timeout: Duration = DEFAULT_TIMEOUT
): R? where T : ResponseNettyPacket, T : CommonResponseType<R> {
    return fireAndAwait(connection, timeout)?.value
}

suspend inline fun <reified T, R> RespondingNettyPacket<T>.awaitUrgent(
    connection: Connection
): R? where T : ResponseNettyPacket, T : CommonResponseType<R> {
    return fireAndAwaitUrgent(connection)?.value
}

suspend inline fun <reified T, R> RespondingNettyPacket<T>.awaitOrThrow(
    connection: Connection,
    timeout: Duration = DEFAULT_TIMEOUT
): R where T : ResponseNettyPacket, T : CommonResponseType<R> {
    return fireAndAwaitOrThrow(connection, timeout).value
}

suspend inline fun <reified T, R> RespondingNettyPacket<T>.awaitOrThrowUrgent(
    connection: Connection
): R where T : ResponseNettyPacket, T : CommonResponseType<R> {
    return fireAndAwaitOrThrowUrgent(connection).value
}

@Suppress("UNCHECKED_CAST")
@InternalApi
@PublishedApi
internal inline fun <reified T, R> extractFactory(): CommonResponseTypeFactory<T, R> where T : ResponseNettyPacket, T : CommonResponseType<R> {
    return T::class.companionObjectInstance as? CommonResponseTypeFactory<T, R>
        ?: error("No factory found for type ${T::class.simpleName}")
}
