package dev.slne.surf.data.bukkit.processor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Role;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Component
public class BukkitListenerProcessor implements BeanPostProcessor {

  @Override
  public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
    if (bean instanceof AopInfrastructureBean) {
      return bean;
    }

    final Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);

    if (!AnnotationUtils.isCandidateClass(targetClass, EventHandler.class)) {
      return bean;
    }

    final Set<Method> eventHandlers = MethodIntrospector.selectMethods(targetClass,
        (Method method) -> AnnotatedElementUtils.isAnnotated(method, EventHandler.class));

    if (!eventHandlers.isEmpty()) {
      registerEventHandlers(beanName, bean, eventHandlers);
    }

    return bean;
  }

  private void registerEventHandlers(String beanName, Object bean, Set<Method> eventHandlerMethods)
      throws BeansException {
    for (final Method handlerMethod : eventHandlerMethods) {
      final Class<?>[] params = handlerMethod.getParameterTypes();

      if (params.length != 1) {
        throw new BeanCreationException(beanName,
            "Event handler method must have exactly one parameter");
      }

      final Class<?> eventParam = params[0];
      if (!eventParam.isAssignableFrom(Event.class)) {
        throw new BeanCreationException(beanName,
            "Event handler method parameter must be a subclass of Event");
      }

      final Class<? extends Event> eventClass = eventParam.asSubclass(Event.class);
      final EventHandler eventHandler = AnnotationUtils.getAnnotation(handlerMethod,
          EventHandler.class);
      assert eventHandler != null : "This method should only be called for event handlers";

      final EventExecutor eventExecutor = (listener, event) -> {
        try {
          if (eventParam.isInstance(event)) {
            handlerMethod.invoke(bean, event);
          }
        } catch (InvocationTargetException | IllegalAccessException e) {
          throw new EventException(e, "Error invoking event handler");
        }
      };

      registerEventHandler(bean, eventClass, eventHandler, eventExecutor);
    }
  }

  private void registerEventHandler(
      Object bean,
      Class<? extends Event> event,
      @NotNull EventHandler eventHandler,
      EventExecutor eventExecutor
  ) {
    final EventPriority priority = eventHandler.priority();
    final boolean ignoreCancelled = eventHandler.ignoreCancelled();

    Bukkit.getPluginManager().registerEvent(
        event,
        new Listener() {
        },
        priority,
        eventExecutor,
        getPluginFromBean(bean),
        ignoreCancelled
    );
  }

  private @NotNull JavaPlugin getPluginFromBean(@NotNull Object bean) {
    return JavaPlugin.getProvidingPlugin(bean.getClass());
  }
}
