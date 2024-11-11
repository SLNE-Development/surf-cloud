package dev.slne.surf.cloud.core.common.netty.registry.listener

import dev.slne.surf.cloud.api.common.netty.exception.SurfNettyListenerRegistrationException
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.core.common.netty.protocol.packet.NettyPacketInfo
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.coroutines.Continuation
import kotlin.reflect.jvm.kotlinFunction

object NettyListenerRegistry {
    private val listeners =
        Object2ObjectOpenHashMap<Class<out NettyPacket>, ObjectSet<RegisteredListener>>()

    fun registerListener(listenerMethod: Method, bean: Any) {
        val params = listenerMethod.parameterTypes
        val isSuspending = listenerMethod.isSuspending()
        val expectedParamSize = if (isSuspending) 3 else 2

        if (params.size !in 1..expectedParamSize) {
            throw SurfNettyListenerRegistrationException(
                "Listener method must have one or two parameters of type NettyPacket and optional NettyPacketInfo"
            )
        }

        var packetClass: Class<out NettyPacket>? = null
        var packetClassIndex = -1
        var packetInfoIndex = -1

        params.forEachIndexed { index, param ->
            when {
                NettyPacket::class.java.isAssignableFrom(param) -> {
                    if (packetClass != null) {
                        throw SurfNettyListenerRegistrationException("Listener method must have only one parameter of type NettyPacket")
                    }
                    packetClass = param as Class<out NettyPacket>
                    packetClassIndex = index
                }

                NettyPacketInfo::class.java.isAssignableFrom(param) -> {
                    if (packetInfoIndex != -1) {
                        throw SurfNettyListenerRegistrationException("Listener method must have only one parameter of type NettyPacketInfo")
                    }
                    packetInfoIndex = index
                }

                Continuation::class.java.isAssignableFrom(param) && isSuspending -> {
                    // Ignore Continuation parameter for suspend functions
                }

                else -> throw SurfNettyListenerRegistrationException(
                    "Listener method must have one or two parameters of type NettyPacket and optional NettyPacketInfo"
                )
            }
        }

        if (packetClass == null) {
            throw SurfNettyListenerRegistrationException("Listener method must have one parameter of type NettyPacket")
        }

        if (!Modifier.isPublic(listenerMethod.modifiers)) {
            throw SurfNettyListenerRegistrationException("Listener method must be public")
        }

        listeners.computeIfAbsent(packetClass, Object2ObjectFunction { ObjectOpenHashSet(1) })
            .add(
                RegisteredListener(
                    bean,
                    listenerMethod,
                    packetClassIndex,
                    packetInfoIndex,
                    isSuspending
                )
            )

//        for (i in params.indices) {
//            val param = params[i]
//            if (NettyPacket::class.java.isAssignableFrom(param)) {
//                if (packetClass != null) {
//                    throw SurfNettyListenerRegistrationException("Listener method must have only one parameter of type NettyPacket")
//                }
//
//                packetClass = param as Class<out NettyPacket<*>>
//                packetClassIndex = i
//            } else if (NettyPacketInfo::class.java.isAssignableFrom(param)) {
//                if (packetInfoIndex != -1) {
//                    throw SurfNettyListenerRegistrationException("Listener method must have only one parameter of type NettyPacketInfo")
//                }
//
//                packetInfoIndex = i
//            } else {
//                throw SurfNettyListenerRegistrationException("Listener method must have one or two parameters of type NettyPacket and optional NettyPacketInfo")
//            }
//        }
//
//        if (packetClass == null) {
//            throw SurfNettyListenerRegistrationException("Listener method must have one parameter of type NettyPacket")
//        }
//
//        if (!Modifier.isPublic(listenerMethod.modifiers)) {
//            throw SurfNettyListenerRegistrationException("Listener method must be public")
//        }
//
//        listeners.computeIfAbsent(packetClass,
//            Object2ObjectFunction {
//                ObjectOpenHashSet(
//                    1
//                )
//            })
//            .add(RegisteredListener(bean, listenerMethod, packetClassIndex, packetInfoIndex))
    }

    fun hasListeners(packetClass: Class<out NettyPacket>) = listeners.containsKey(packetClass)
    fun getListeners(packetClass: Class<out NettyPacket>) = listeners[packetClass]

    private fun Method.isSuspending() = kotlinFunction?.isSuspend == true
}
