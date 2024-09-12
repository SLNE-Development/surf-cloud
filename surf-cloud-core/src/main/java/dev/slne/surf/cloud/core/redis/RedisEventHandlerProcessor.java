package dev.slne.surf.cloud.core.redis;

import dev.slne.surf.cloud.api.redis.RedisEvent;
import dev.slne.surf.cloud.api.redis.RedisEventHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.flogger.Flogger;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import tech.hiddenproject.aide.reflection.LambdaWrapperHolder;
import tech.hiddenproject.aide.reflection.WrapperHolder;
import tech.hiddenproject.aide.reflection.annotation.Invoker;

@Component
@Validated
public class RedisEventHandlerProcessor implements BeanPostProcessor {

  private final ObjectProvider<RedisMessageListenerContainer> container;
  private final ObjectProvider<GenericJackson2JsonRedisSerializer> redisSerializer;

  @Autowired
  public RedisEventHandlerProcessor(
      ObjectProvider<RedisMessageListenerContainer> container,
      ObjectProvider<GenericJackson2JsonRedisSerializer> redisSerializer
  ) {
    this.container = container;
    this.redisSerializer = redisSerializer;

    final LambdaWrapperHolder holder = LambdaWrapperHolder.DEFAULT;
    holder.add(RedisEventHandlerInvoker.class);
  }

  @Override
  public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName)
      throws BeansException {
    final Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);

    if (!AnnotationUtils.isCandidateClass(targetClass, RedisEventHandler.class)) {
      return bean;
    }

    final Set<Method> eventHandlers = MethodIntrospector.selectMethods(targetClass,
        (Method method) -> AnnotatedElementUtils.isAnnotated(method, RedisEventHandler.class));

    if (!eventHandlers.isEmpty()) {
      registerEventHandlers(beanName, bean, eventHandlers);
    }

    return bean;
  }

  private void registerEventHandlers(
      String beanName,
      Object bean,
      @NotNull Set<Method> eventHandlerMethods
  ) {
    for (final Method handlerMethod : eventHandlerMethods) {
      final RedisEventHandler eventHandler = AnnotationUtils.getAnnotation(handlerMethod,
          RedisEventHandler.class);
      assert eventHandler != null : "This method should only be called for event handlers";

      final RedisEventHandlerListenerAdapter adapter = new RedisEventHandlerListenerAdapter(
          beanName, bean, handlerMethod, redisSerializer.getObject());

      container.getObject().addMessageListener(adapter, Arrays.stream(eventHandler.channels())
          .map(ChannelTopic::of)
          .toList());
    }
  }

  @Flogger
  private static class RedisEventHandlerListenerAdapter implements MessageListener {

    private final String beanName;
    private final Class<?> eventType;
    private final RedisEventHandlerInvoker invoker;
    private final GenericJackson2JsonRedisSerializer redisSerializer;
    private final Object bean;

    public RedisEventHandlerListenerAdapter(
        String beanName,
        Object bean,
        @NotNull Method eventHandlerMethod,
        GenericJackson2JsonRedisSerializer redisSerializer
    ) {
      this.beanName = beanName;
      this.bean = bean;
      this.eventType = eventHandlerMethod.getParameterTypes()[0];
      this.redisSerializer = redisSerializer;
      this.invoker = createInvoker(eventHandlerMethod);
    }

    @Override
    public void onMessage(@NotNull Message message, byte[] pattern) {
      final Object receivedEvent = redisSerializer.deserialize(message.getBody(), eventType);

      if (receivedEvent == null) {
        return;
      }

      if (eventType.isAssignableFrom(receivedEvent.getClass())) {
        try {
          invoker.handle(bean, (RedisEvent) receivedEvent);
        } catch (Throwable e) {
          log.atWarning()
              .atMostEvery(1, TimeUnit.SECONDS)
              .withCause(new RuntimeException("Failed to invoke event handler method", e))
              .log("Encountered an exception while handling event in bean %s", beanName);
        }
      }
    }
  }

  private static RedisEventHandlerInvoker createInvoker(Method method) {
    final LambdaWrapperHolder holder = LambdaWrapperHolder.DEFAULT;
    final WrapperHolder<RedisEventHandlerInvoker> methodHolder = holder.wrap(method,
        RedisEventHandlerInvoker.class);
    return methodHolder.getWrapper();
  }

  @FunctionalInterface
  public interface RedisEventHandlerInvoker {

    @Invoker
    void handle(Object caller, RedisEvent event);
  }
}
