package dev.slne.surf.cloud.core.netty.common.registry.packet

import dev.slne.surf.cloud.api.netty.exception.SurfNettyRegisterPacketException
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import tech.hiddenproject.aide.reflection.LambdaWrapper
import tech.hiddenproject.aide.reflection.LambdaWrapperHolder
import tech.hiddenproject.aide.reflection.MethodHolder
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaConstructor

internal class RegisteredPacket(packetClass: KClass<out NettyPacket<*>>) {
    private val fastConstructor: MethodHolder<LambdaWrapper, Void, out NettyPacket<*>>

    init {
        val constructor = packetClass.primaryConstructor?.javaConstructor
            ?: throw SurfNettyRegisterPacketException("Packet class must have a public no-args constructor")
        this.fastConstructor = LambdaWrapperHolder.DEFAULT.wrapSafe(constructor)
    }

    fun createPacket(): NettyPacket<*> = fastConstructor.invokeStatic()
    inline fun <reified T> createPacket(): T {
        val packet = createPacket()
        require(packet is T) { "Packet is not of type ${T::class.simpleName}" }

        return packet
    }
}
