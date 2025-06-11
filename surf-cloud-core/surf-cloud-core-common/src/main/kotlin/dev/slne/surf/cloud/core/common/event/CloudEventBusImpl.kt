package dev.slne.surf.cloud.core.common.event

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.event.Cancellable
import dev.slne.surf.cloud.api.common.event.CloudEvent
import dev.slne.surf.cloud.api.common.event.CloudEventBus
import dev.slne.surf.cloud.api.common.event.CloudEventHandler
import dev.slne.surf.cloud.api.common.util.isSuspending
import dev.slne.surf.cloud.api.common.util.mutableObject2ObjectMapOf
import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.api.common.util.synchronize
import dev.slne.surf.cloud.core.common.coroutines.CloudEventBusScope
import dev.slne.surf.surfapi.core.api.util.findAnnotation
import it.unimi.dsi.fastutil.objects.ObjectList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.modifier.FieldManifestation
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy
import net.bytebuddy.implementation.FieldAccessor
import net.bytebuddy.implementation.MethodCall
import net.bytebuddy.implementation.bytecode.assign.Assigner
import net.bytebuddy.matcher.ElementMatchers
import org.springframework.core.ResolvableType
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import java.lang.reflect.Method
import kotlin.reflect.full.callSuspend
import kotlin.reflect.jvm.kotlinFunction

@AutoService(CloudEventBus::class)
class CloudEventBusImpl : CloudEventBus {
    private val listeners =
        mutableObject2ObjectMapOf<Class<out CloudEvent>, ObjectList<ListenerWrapper>>().synchronize()
    private val spelParser = SpelExpressionParser()

    override fun register(listener: Any) {
        val cls = listener.javaClass
        register(listener, cls.declaredMethods.asIterable())
    }

    fun register(listener: Any, methods: Iterable<Method>) {
        for (method in methods) {
            val annotation = method.findAnnotation<CloudEventHandler>() ?: continue
            val isSuspend = method.isSuspending()
            require((isSuspend && method.parameterCount == 2) || (!isSuspend && method.parameterCount == 1)) { "Invalid method signature for @CloudEventHandler: ${method.name} in ${listener.javaClass.name}! Method must take exactly one parameter of type ${CloudEvent::class.simpleName}" }

            val methodType = ResolvableType.forMethodParameter(method, 0)
            val rawType = method.parameterTypes[0].asSubclass(CloudEvent::class.java)

            val invoker = createInvoker(listener, method)
            val listeners =
                listeners.computeIfAbsent(rawType) { mutableObjectListOf<ListenerWrapper>().synchronize() }

            val expression = if (annotation.condition.isNotBlank()) {
                spelParser.parseExpression(annotation.condition)
            } else null

            listeners += ListenerWrapper(
                eventType = rawType,
                genericType = methodType,
                invoker = invoker,
                priority = annotation.priority,
                ignoreCancelled = annotation.ignoreCancelled,
                condition = expression,
            )
            listeners.sort()
        }
    }

    override fun unregister(listener: Any) {
        listeners.values.forEach { lst ->
            lst.removeIf {
                (it.invoker as? GeneratedInvoker)?.owner === listener ||
                        (it.invoker as? ReflectionInvoker)?.instance === listener
            }
        }
    }

    override suspend fun post(event: CloudEvent) = withContext(CloudEventBusScope.context) {
        val eventType = ResolvableType.forInstance(event)
        val toCall = listeners
            .filterKeys { it.isAssignableFrom(event.javaClass) }
            .values.asSequence()
            .flatten()
            .filter { listener ->
                eventType.isAssignableFrom(listener.genericType)
            }
            .sorted()

        for (wrapper in toCall) {
            val cancelled = event is Cancellable && event.isCancelled
            if (cancelled && !wrapper.ignoreCancelled) continue

            val condition = wrapper.condition
            if (condition != null) {
                val ctx = StandardEvaluationContext()
                ctx.setVariable("event", event)

                val result = condition.getValue(ctx, Boolean::class.java) ?: true
                if (!result) continue
            }

            wrapper.invoker.invoke(event)
        }
    }

    private fun createInvoker(instance: Any, method: Method): EventListenerInvoker {
        return if (method.isSuspending()) {
            ReflectionInvoker(instance, method)
        } else {
            byteBuddyInvoker(instance, method)
        }
    }

    private fun byteBuddyInvoker(instance: Any, method: Method): EventListenerInvoker {
        val generated = ByteBuddy()
            .subclass(EventListenerInvoker::class.java)
            .implement(GeneratedInvoker::class.java)
            .defineField("owner", instance.javaClass, Visibility.PRIVATE, FieldManifestation.FINAL)
            .defineMethod("getOwner", Any::class.java, Visibility.PUBLIC)
            .intercept(FieldAccessor.ofField("owner"))
            .defineConstructor(Visibility.PUBLIC).withParameters(instance.javaClass)
            .intercept(
                MethodCall.invoke(Object::class.java.getConstructor())
                    .andThen(FieldAccessor.ofField("owner").setsArgumentAt(0))
            )
            .method(ElementMatchers.named("invoke"))
            .intercept(
                MethodCall.invoke(method)
                    .onField("owner")
                    .withArgument(0)
                    .withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC)
            )
            .make()
            .load(instance.javaClass.classLoader, ClassLoadingStrategy.Default.INJECTION)
            .loaded

        val ctor = generated.getConstructor(instance.javaClass)
        return ctor.newInstance(instance) as EventListenerInvoker
    }

    private class ReflectionInvoker(val instance: Any, val method: Method) : EventListenerInvoker {
        init {
            method.isAccessible = true
        }

        override suspend fun invoke(event: CloudEvent) {
            CloudEventBusScope.launch {
                val kfn = method.kotlinFunction ?: error("Not a Kotlin function")
                kfn.callSuspend(instance, event)
            }.join()
        }
    }

    internal interface GeneratedInvoker {
        val owner: Any
    }
}

val cloudEventBusImpl = CloudEventBus.instance as CloudEventBusImpl