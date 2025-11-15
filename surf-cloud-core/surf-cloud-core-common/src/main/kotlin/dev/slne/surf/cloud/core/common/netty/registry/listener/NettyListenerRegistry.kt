package dev.slne.surf.cloud.core.common.netty.registry.listener

import dev.slne.surf.cloud.api.common.netty.exception.SurfNettyListenerRegistrationException
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacketInfo
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.util.isSuspending
import dev.slne.surf.cloud.core.common.coroutines.PacketHandlerIoScope
import dev.slne.surf.cloud.core.common.coroutines.PacketHandlerScope
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import io.netty.channel.Channel
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.launch
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.coroutines.Continuation

object NettyListenerRegistry {
    private val listeners =
        mutableObject2ObjectMapOf<Class<out NettyPacket>, ObjectSet<RegisteredListener>>()

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
                    @Suppress("UNCHECKED_CAST")
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

        val mode = HandlerModeResolver.forMethod(listenerMethod, packetClass, isSuspending)

        listeners.computeIfAbsent(packetClass) { ObjectOpenHashSet(1) }
            .add(
                RegisteredListener(
                    bean,
                    listenerMethod,
                    packetClassIndex,
                    packetInfoIndex,
                    isSuspending,
                    mode
                )
            )
    }

    fun hasListeners(packetClass: Class<out NettyPacket>) = listeners.containsKey(packetClass)
    fun getListeners(packetClass: Class<out NettyPacket>) = listeners[packetClass]

    fun dispatch(
        channel: Channel,
        packet: NettyPacket,
        info: NettyPacketInfo,
        handleException: (e: Throwable, listener: RegisteredListener) -> Unit
    ) {
        val listeners = getListeners(packet.javaClass) ?: return
        val eventLoop = channel.eventLoop()

        for (listener in listeners) {
            try {
                when (listener.mode) {
                    PacketHandlerMode.NETTY -> {
                        if (eventLoop.inEventLoop()) {
                            listener.handleOnNetty(packet, info)
                        } else {
                            eventLoop.execute { listener.handleOnNetty(packet, info) }
                        }
                    }

                    PacketHandlerMode.DEFAULT -> PacketHandlerScope.launch {
                        listener.handle(packet, info)
                    }

                    PacketHandlerMode.IO -> PacketHandlerIoScope.launch {
                        listener.handle(packet, info)
                    }

                    PacketHandlerMode.INHERIT -> error("PacketHandlerMode.INHERIT is not allowed for dispatch")
                }
            } catch (e: Throwable) {
                handleException(e, listener)
            }
        }
    }
}
