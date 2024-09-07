package dev.slne.surf.data.core.redis;

import dev.slne.surf.data.api.redis.RedisEvent;
import dev.slne.surf.data.api.redis.RedisEventHandler;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.flogger.Flogger;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class RedisEventHandlerProcessor implements BeanPostProcessor {

  private final ObjectProvider<RedisMessageListenerContainer> container;
  private final ObjectProvider<ReactiveRedisTemplate<String, Object>> redisTemplate;

  @Autowired
  public RedisEventHandlerProcessor(
      ObjectProvider<RedisMessageListenerContainer> container,
      ObjectProvider<ReactiveRedisTemplate<String, Object>> redisTemplate
  ) {
    this.container = container;
    this.redisTemplate = redisTemplate;
  }

  @Override
  public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
    final Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);

    if (!AnnotationUtils.isCandidateClass(targetClass, RedisEventHandler.class)) {
      return bean;
    }

    final Set<Method> eventHandlers = MethodIntrospector.selectMethods(targetClass,
        (Method method) -> AnnotatedElementUtils.isAnnotated(method, RedisEventHandler.class));

    if (!eventHandlers.isEmpty()) {
      registerEventHandlers(beanName, eventHandlers);
    }

    return bean;
  }

  private void registerEventHandlers(
      String beanName,
      @NotNull Set<Method> eventHandlerMethods
  ) {
    for (final Method handlerMethod : eventHandlerMethods) {
      final RedisEventHandler eventHandler = AnnotationUtils.getAnnotation(handlerMethod,
          RedisEventHandler.class);
      assert eventHandler != null : "This method should only be called for event handlers";

      final RedisEventHandlerListenerAdapter adapter = new RedisEventHandlerListenerAdapter(beanName,
          handlerMethod, redisTemplate.getObject());

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
    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    public RedisEventHandlerListenerAdapter(
        String beanName,
        @NotNull Method eventHandlerMethod,
        ReactiveRedisTemplate<String, Object> redisTemplate
    ) {
      this.beanName = beanName;
      this.redisTemplate = redisTemplate;
      this.eventType = eventHandlerMethod.getParameterTypes()[0];
      this.invoker = createInvoker(beanName, eventHandlerMethod, eventType);
    }

    @Override
    public void onMessage(@NotNull Message message, byte[] pattern) {
      final Object receivedEvent = redisTemplate.getSerializationContext()
          .getValueSerializationPair().read(ByteBuffer.wrap(message.getBody()));

      if (receivedEvent == null) {
        return;
      }

      if (eventType.isAssignableFrom(receivedEvent.getClass())) {
        try {
          invoker.handle((RedisEvent) receivedEvent);
        } catch (Throwable e) {
          log.atWarning()
              .atMostEvery(1, TimeUnit.SECONDS)
              .withCause(new RuntimeException("Failed to invoke event handler method", e))
              .log("Encountered an exception while handling event in bean %s", beanName);
        }
      }
    }
  }

  private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

  private static RedisEventHandlerInvoker createInvoker(
      String beanName,
      Method method,
      Class<?> eventType
  ) {
    try {
      final MethodHandle methodHandle = lookup.unreflect(method);

      final CallSite callSite = LambdaMetafactory.metafactory(
          lookup,
          "handle",
          MethodType.methodType(RedisEventHandlerInvoker.class),
          MethodType.methodType(void.class, Object.class),
          methodHandle,
          MethodType.methodType(void.class, eventType)
      );

      return (RedisEventHandlerInvoker) callSite.getTarget().invokeExact();
    } catch (Throwable e) {
      throw new BeanCreationException(beanName, "Failed to create invoker for method " + method, e);
    }
  }

  @FunctionalInterface
  private interface RedisEventHandlerInvoker {

    void handle(RedisEvent event);
  }
}
