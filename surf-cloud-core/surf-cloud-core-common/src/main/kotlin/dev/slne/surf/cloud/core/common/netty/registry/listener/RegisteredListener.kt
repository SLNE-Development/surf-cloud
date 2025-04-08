package dev.slne.surf.cloud.core.common.netty.registry.listener

import dev.slne.surf.cloud.api.common.netty.exception.SurfNettyListenerRegistrationException
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacketInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.hiddenproject.aide.reflection.LambdaWrapperHolder
import tech.hiddenproject.aide.reflection.annotation.Invoker
import java.lang.reflect.Method

class RegisteredListener(
    private val bean: Any,
    listenerMethod: Method,
    packetClassIndex: Int,
    packetInfoIndex: Int,
    private val suspending: Boolean
) {
    private var invoker: Any
    private val invokerType: InvokerType

    init {
        val params = listenerMethod.parameterTypes

        if (params.size == 1 || (params.size == 2 && suspending)) { // Only NettyPacket
            val clazz =
                if (suspending) RegisteredListenerSuspendInvoker1::class else RegisteredListenerInvoker1::class

            this.invoker = LambdaWrapperHolder.DEFAULT.wrap(listenerMethod, clazz.java).wrapper
            this.invokerType = InvokerType.ONE_PARAM
        } else if (params.size == 2 || (params.size == 3 && suspending)) { // NettyPacket and NettyPacketInfo
            if (packetClassIndex == 0 && packetInfoIndex == 1) { // Normal order (NettyPacket, NettyPacketInfo)
                val clazz =
                    if (suspending) RegisteredListenerSuspendInvoker2::class else RegisteredListenerInvoker2::class

                this.invoker = LambdaWrapperHolder.DEFAULT.wrap(listenerMethod, clazz.java).wrapper
                this.invokerType = InvokerType.TWO_PARAMS
            } else if (packetInfoIndex == 0 && packetClassIndex == 1) { // Reversed order (NettyPacketInfo, NettyPacket)
                val clazz =
                    if (suspending) RegisteredListenerSuspendInvoker2Rev::class else RegisteredListenerInvoker2Rev::class

                this.invoker = LambdaWrapperHolder.DEFAULT.wrap(listenerMethod, clazz.java).wrapper
                this.invokerType = InvokerType.TWO_PARAMS_REVERSED
            } else {
                throw SurfNettyListenerRegistrationException("Invalid parameter order")
            }
        } else {
            throw SurfNettyListenerRegistrationException("Invalid number of parameters")
        }
    }

    suspend fun handle(packet: NettyPacket, info: NettyPacketInfo) =
        withContext(Dispatchers.IO) {
            when (invokerType) {
                InvokerType.ONE_PARAM -> {
                    if (suspending) {
                        (invoker as RegisteredListenerSuspendInvoker1).handle(bean, packet)
                    } else {
                        (invoker as RegisteredListenerInvoker1).handle(bean, packet)
                    }
                }

                InvokerType.TWO_PARAMS -> {
                    if (suspending) {
                        (invoker as RegisteredListenerSuspendInvoker2).handle(bean, packet, info)
                    } else {
                        (invoker as RegisteredListenerInvoker2).handle(bean, packet, info)
                    }
                }

                InvokerType.TWO_PARAMS_REVERSED -> {
                    if (suspending) {
                        (invoker as RegisteredListenerSuspendInvoker2Rev).handle(bean, info, packet)
                    } else {
                        (invoker as RegisteredListenerInvoker2Rev).handle(bean, info, packet)
                    }
                }
            }
        }

    private enum class InvokerType {
        ONE_PARAM,
        TWO_PARAMS,
        TWO_PARAMS_REVERSED
    }

    // region Standard Invokers
    fun interface RegisteredListenerInvoker1 {
        @Invoker
        fun handle(caller: Any, packet: NettyPacket)
    }

    fun interface RegisteredListenerInvoker2 {
        @Invoker
        fun handle(caller: Any, packet: NettyPacket, info: NettyPacketInfo)
    }

    fun interface RegisteredListenerInvoker2Rev {
        @Invoker
        fun handle(caller: Any, info: NettyPacketInfo, packet: NettyPacket)
    }
    // endregion

    // region Suspend Invokers for Kotlin coroutines
    fun interface RegisteredListenerSuspendInvoker1 {
        @Invoker
        suspend fun handle(caller: Any, packet: NettyPacket)
    }

    fun interface RegisteredListenerSuspendInvoker2 {
        @Invoker
        suspend fun handle(caller: Any, packet: NettyPacket, info: NettyPacketInfo)
    }

    fun interface RegisteredListenerSuspendInvoker2Rev {
        @Invoker
        suspend fun handle(caller: Any, info: NettyPacketInfo, packet: NettyPacket)
    }
    // endregion

    companion object {
        init {
            LambdaWrapperHolder.DEFAULT.add(RegisteredListenerInvoker1::class.java)
            LambdaWrapperHolder.DEFAULT.add(RegisteredListenerInvoker2::class.java)
            LambdaWrapperHolder.DEFAULT.add(RegisteredListenerInvoker2Rev::class.java)
            LambdaWrapperHolder.DEFAULT.add(RegisteredListenerSuspendInvoker1::class.java)
            LambdaWrapperHolder.DEFAULT.add(RegisteredListenerSuspendInvoker2::class.java)
            LambdaWrapperHolder.DEFAULT.add(RegisteredListenerSuspendInvoker2Rev::class.java)
        }
    }
}
