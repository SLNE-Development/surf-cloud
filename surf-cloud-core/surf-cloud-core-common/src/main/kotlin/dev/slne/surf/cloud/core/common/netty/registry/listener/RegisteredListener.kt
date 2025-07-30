package dev.slne.surf.cloud.core.common.netty.registry.listener

import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacketInfo
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.method.MethodDescription
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy
import net.bytebuddy.implementation.FieldAccessor
import net.bytebuddy.implementation.MethodCall
import net.bytebuddy.implementation.bytecode.assign.Assigner
import net.bytebuddy.matcher.ElementMatchers
import java.lang.reflect.Method
import java.lang.reflect.Modifier

class RegisteredListener(
    private val bean: Any,
    private val listenerMethod: Method,
    packetClassIndex: Int,
    packetInfoIndex: Int,
    private val suspending: Boolean
) {
    val owner: Any get() = bean

    private val invoker: Any
    private val invokerType: InvokerType

    init {
        val params = listenerMethod.parameterTypes
        val beanLoader: ClassLoader = bean.javaClass.classLoader

        if (params.size == 1 || (params.size == 2 && suspending)) {
            invokerType = InvokerType.ONE_PARAM
            invoker = generateInvoker(
                iface = if (suspending) RegisteredListenerSuspendInvoker1::class.java else RegisteredListenerInvoker1::class.java,
                loader = beanLoader,
                ownerArg = bean,
                paramOrder = if (suspending) intArrayOf(1, 2) else intArrayOf(1)
            )
        } else if (params.size == 2 || (params.size == 3 && suspending)) {
            val reversed = packetInfoIndex == 0 && packetClassIndex == 1
            invokerType = if (reversed) InvokerType.TWO_PARAMS_REVERSED else InvokerType.TWO_PARAMS
            invoker = generateInvoker(
                iface = when {
                    suspending && !reversed -> RegisteredListenerSuspendInvoker2::class.java
                    suspending && reversed -> RegisteredListenerSuspendInvoker2Rev::class.java
                    !suspending && !reversed -> RegisteredListenerInvoker2::class.java
                    else -> RegisteredListenerInvoker2Rev::class.java
                },
                loader = beanLoader,
                ownerArg = bean,
                paramOrder = when {
                    suspending && !reversed -> intArrayOf(1, 2, 3)  // packet, info, cont
                    suspending && reversed -> intArrayOf(2, 1, 3)  // info, packet, cont
                    !suspending && !reversed -> intArrayOf(1, 2)    // packet, info
                    else -> intArrayOf(2, 1)    // info, packet
                }
            )
        } else {
            error("Invalid number of parameters for listener method: ${listenerMethod.name} in ${bean.javaClass.name}. Method must take exactly one parameter of type NettyPacket or two parameters of type NettyPacket and NettyPacketInfo.")
        }
    }

    suspend fun handle(packet: NettyPacket, info: NettyPacketInfo) {
        when (invokerType) {
            InvokerType.ONE_PARAM -> if (suspending) {
                (invoker as RegisteredListenerSuspendInvoker1).handle(bean, packet)
            } else {
                (invoker as RegisteredListenerInvoker1).handle(bean, packet)
            }

            InvokerType.TWO_PARAMS -> if (suspending) {
                (invoker as RegisteredListenerSuspendInvoker2).handle(bean, packet, info)
            } else {
                (invoker as RegisteredListenerInvoker2).handle(bean, packet, info)
            }

            InvokerType.TWO_PARAMS_REVERSED -> if (suspending) {
                (invoker as RegisteredListenerSuspendInvoker2Rev).handle(bean, info, packet)
            } else {
                (invoker as RegisteredListenerInvoker2Rev).handle(bean, info, packet)
            }
        }

    }

    private fun <I> generateInvoker(
        iface: Class<I>,
        loader: ClassLoader,
        ownerArg: Any,
        paramOrder: IntArray
    ): I {
        val baseCall = if (Modifier.isStatic(listenerMethod.modifiers)) {
            MethodCall.invoke(listenerMethod).on(listenerMethod.declaringClass)
        } else {
            MethodCall.invoke(listenerMethod).onArgument(0)
        }
        val callWithArgs = paramOrder.fold(baseCall) { acc, idx -> acc.withArgument(idx) }
        val finalCall = callWithArgs.withAssigner(
            Assigner.DEFAULT,
            Assigner.Typing.DYNAMIC
        )

        val dynamicType = ByteBuddy()
            .subclass(Any::class.java)
            .implement(iface)
            .defineField("owner", Any::class.java, Modifier.PRIVATE or Modifier.FINAL)
            .defineConstructor(Modifier.PUBLIC)
            .withParameters(Any::class.java)
            .intercept(
                MethodCall.invoke(Object::class.java.getConstructor())
                    .andThen(FieldAccessor.ofField("owner").setsArgumentAt(0))
            )
            .method(
                ElementMatchers.named<MethodDescription>("handle")
                    .and(ElementMatchers.isDeclaredBy(iface))
            )
            .intercept(finalCall)
            .make()
            .load(loader, ClassLoadingStrategy.Default.INJECTION)
            .loaded

        val constructor = dynamicType.getDeclaredConstructor(Any::class.java)

        @Suppress("UNCHECKED_CAST")
        return constructor.newInstance(ownerArg) as I
    }

    private enum class InvokerType {
        ONE_PARAM,
        TWO_PARAMS,
        TWO_PARAMS_REVERSED
    }

    fun interface RegisteredListenerInvoker1 {
        fun handle(caller: Any, packet: NettyPacket)
    }

    fun interface RegisteredListenerInvoker2 {
        fun handle(caller: Any, packet: NettyPacket, info: NettyPacketInfo)
    }

    fun interface RegisteredListenerInvoker2Rev {
        fun handle(caller: Any, info: NettyPacketInfo, packet: NettyPacket)
    }

    fun interface RegisteredListenerSuspendInvoker1 {
        suspend fun handle(caller: Any, packet: NettyPacket)
    }

    fun interface RegisteredListenerSuspendInvoker2 {
        suspend fun handle(caller: Any, packet: NettyPacket, info: NettyPacketInfo)
    }

    fun interface RegisteredListenerSuspendInvoker2Rev {
        suspend fun handle(caller: Any, info: NettyPacketInfo, packet: NettyPacket)
    }
}