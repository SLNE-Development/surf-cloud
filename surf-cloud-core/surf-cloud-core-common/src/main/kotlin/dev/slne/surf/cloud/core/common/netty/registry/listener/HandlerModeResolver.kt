package dev.slne.surf.cloud.core.common.netty.registry.listener

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.meta.SurfNettyPacketHandler
import dev.slne.surf.cloud.api.common.netty.exception.SurfNettyListenerRegistrationException
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import org.springframework.core.annotation.AnnotationUtils
import java.lang.reflect.Method

object HandlerModeResolver {
    fun forPacket(packetClass: Class<out NettyPacket>): PacketHandlerMode {
        val annotation = AnnotationUtils.getAnnotation(packetClass, SurfNettyPacket::class.java)
        return annotation?.handlerMode ?: PacketHandlerMode.DEFAULT
    }

    fun forMethod(
        method: Method,
        packetClass: Class<out NettyPacket>,
        isSuspending: Boolean
    ): PacketHandlerMode {
        val methodAnnotation =
            AnnotationUtils.getAnnotation(method, SurfNettyPacketHandler::class.java)
        val methodMode = methodAnnotation?.mode ?: PacketHandlerMode.INHERIT
        val resolvedMethodMode =
            if (methodMode == PacketHandlerMode.INHERIT) forPacket(packetClass) else methodMode

        if (resolvedMethodMode == PacketHandlerMode.NETTY && isSuspending) {
            throw SurfNettyListenerRegistrationException("Listener ${method.declaringClass.name}#${method.name} is suspend, but mode=NETTY forbids suspend functions.")
        }

        return resolvedMethodMode
    }
}