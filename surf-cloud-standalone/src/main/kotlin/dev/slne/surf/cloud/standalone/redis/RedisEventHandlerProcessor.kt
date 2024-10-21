package dev.slne.surf.cloud.standalone.redis

import dev.slne.surf.cloud.api.util.*
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.stereotype.Component
import tech.hiddenproject.aide.reflection.LambdaWrapperHolder
import tech.hiddenproject.aide.reflection.annotation.Invoker
import java.lang.reflect.Method
import java.util.concurrent.TimeUnit

@Component
class RedisEventHandlerProcessor @Autowired constructor(
    containerProvider: ObjectProvider<RedisMessageListenerContainer>,
    redisSerializerProvider: ObjectProvider<GenericJackson2JsonRedisSerializer>
) : BeanPostProcessor {
    private val container by containerProvider
    private val redisSerializer by redisSerializerProvider

    init {
        LambdaWrapperHolder.DEFAULT.add(RedisEventHandlerInvoker::class.java)
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        val targetClass = bean.ultimateTargetClass()
        if (!targetClass.isCandidateFor<RedisEventHandler>()) return bean

        val eventHandlers = targetClass.selectFunctions { it.isAnnotated<RedisEventHandler>() }

        if (eventHandlers.isNotEmpty()) {
            registerEventHandlers(beanName, bean, eventHandlers)
        }

        return bean
    }

    private fun registerEventHandlers(
        beanName: String,
        bean: Any,
        eventHandlerMethods: Set<Method>
    ) {
        for (handlerMethod in eventHandlerMethods) {
            val adapter =
                RedisEventHandlerListenerAdapter(beanName, bean, handlerMethod, redisSerializer)
            val eventHandler = handlerMethod.getAnnotation(RedisEventHandler::class.java)

            container.addMessageListener(adapter, eventHandler.channels.map { ChannelTopic.of(it) })
        }
    }


    private class RedisEventHandlerListenerAdapter(
        private val beanName: String,
        private val bean: Any,
        eventHandlerMethod: Method,
        private val redisSerializer: GenericJackson2JsonRedisSerializer
    ) : MessageListener {
        private val log = logger()
        private val eventType = eventHandlerMethod.parameterTypes[0]
        private val invoker = createInvoker(eventHandlerMethod)

        override fun onMessage(message: Message, pattern: ByteArray?) {
            val receivedEvent = redisSerializer.deserialize(message.body, eventType) ?: return

            if (eventType.isAssignableFrom(receivedEvent.javaClass)) {
                try {
                    invoker.handle(bean, receivedEvent as RedisEvent)
                } catch (e: Throwable) {
                    log.atWarning()
                        .atMostEvery(1, TimeUnit.SECONDS)
                        .withCause(RuntimeException("Failed to invoke event handler method", e))
                        .log("Encountered an exception while handling event in bean %s", beanName)
                }
            }
        }
    }

    fun interface RedisEventHandlerInvoker {
        @Invoker
        fun handle(caller: Any, event: RedisEvent)
    }
}

private fun createInvoker(method: Method) =
    LambdaWrapperHolder.DEFAULT.wrap(method, RedisEventHandlerProcessor.RedisEventHandlerInvoker::class.java).wrapper
