package dev.slne.surf.cloud.core.common.event

import com.github.benmanes.caffeine.cache.Caffeine
import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.event.Cancellable
import dev.slne.surf.cloud.api.common.event.CloudEvent
import dev.slne.surf.cloud.api.common.event.CloudEventBus
import dev.slne.surf.cloud.api.common.event.CloudEventHandler
import dev.slne.surf.cloud.api.common.util.isSuspending
import dev.slne.surf.cloud.core.common.coroutines.CloudEventBusScope
import dev.slne.surf.surfapi.core.api.util.findAnnotation
import dev.slne.surf.surfapi.core.api.util.logger
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
    private val log = logger()

    private val listenerHandler = Caffeine.newBuilder()
        .build<Class<out CloudEvent>, CloudEventListenerHolder> { CloudEventListenerHolder() }

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
            val handler = listenerHandler.get(rawType)

            val expression = if (annotation.condition.isNotBlank()) {
                spelParser.parseExpression(annotation.condition)
            } else null

            handler.register(
                ListenerWrapper(
                    eventType = rawType,
                    genericType = methodType,
                    invoker = invoker,
                    priority = annotation.priority,
                    ignoreCancelled = annotation.ignoreCancelled,
                    condition = expression,
                )
            )
        }
    }

    override fun unregister(listener: Any) {
        listenerHandler.asMap().values.forEach { it.unregister(listener) }
        listenerHandler.asMap().entries.removeIf { it.value.isEmpty() }
    }

    override fun postAndForget(event: CloudEvent) {
        CloudEventBusScope.launch { post(event) }
    }

    override suspend fun post(event: CloudEvent) = withContext(CloudEventBusScope.context) {
        val eventType = ResolvableType.forInstance(event)
        listenerHandler.asMap()
            .filterKeys { it.isAssignableFrom(event.javaClass) }
            .values
            .asSequence()
            .flatMap { it.getListeners(eventType) }
            .sorted()
            .forEach { wrapper ->
                val cancelled = event is Cancellable && event.isCancelled
                if (cancelled && !wrapper.ignoreCancelled) return@forEach

                val condition = wrapper.condition
                if (condition != null) {
                    val ctx = StandardEvaluationContext()
                    ctx.setVariable("event", event)

                    val result = condition.getValue(ctx, Boolean::class.java) ?: true
                    if (!result) return@forEach
                }

                try {
                    wrapper.invoker.invoke(event)
                } catch (e: Throwable) {
                    log.atWarning()
                        .withCause(e)
                        .log("Error while invoking event listener for ${event.javaClass.name} in ${wrapper.invoker.owner.javaClass.name}#${wrapper.invoker.owner.hashCode()}")
                }
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

    class ReflectionInvoker(override val owner: Any, val method: Method) : EventListenerInvoker {
        init {
            method.isAccessible = true
        }

        override suspend fun invoke(event: CloudEvent) {
            CloudEventBusScope.launch {
                val kfn = method.kotlinFunction ?: error("Not a Kotlin function")
                kfn.callSuspend(owner, event)
            }.join()
        }
    }

    internal interface GeneratedInvoker // This interface is used to mark generated invokers
}

val cloudEventBusImpl = CloudEventBus.instance as CloudEventBusImpl