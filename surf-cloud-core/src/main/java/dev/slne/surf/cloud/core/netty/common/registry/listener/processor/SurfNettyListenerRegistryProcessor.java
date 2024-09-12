package dev.slne.surf.cloud.core.netty.common.registry.listener.processor;

import dev.slne.surf.cloud.api.meta.SurfNettyPacketHandler;
import dev.slne.surf.cloud.api.netty.exception.SurfNettyListenerRegistrationException;
import dev.slne.surf.cloud.core.netty.common.registry.listener.SurfNettyListenerRegistry;
import java.lang.reflect.Method;
import java.util.Set;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Component
public class SurfNettyListenerRegistryProcessor implements BeanPostProcessor {

  private final ObjectProvider<SurfNettyListenerRegistry> surfNettyListenerRegistry;

  public SurfNettyListenerRegistryProcessor(ObjectProvider<SurfNettyListenerRegistry> surfNettyListenerRegistry) {
    this.surfNettyListenerRegistry = surfNettyListenerRegistry;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (bean instanceof AopInfrastructureBean) {
      return bean;
    }

    final Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);

    if (!AnnotationUtils.isCandidateClass(targetClass, SurfNettyPacketHandler.class)) {
      return bean;
    }

    final Set<Method> nettyHandlers = MethodIntrospector.selectMethods(targetClass,
        (Method method) -> AnnotatedElementUtils.isAnnotated(method, SurfNettyPacketHandler.class));

    if (!nettyHandlers.isEmpty()) {
      registerNettyHandlers(beanName, bean, nettyHandlers);
    }

    return bean;
  }

  private void registerNettyHandlers(String beanName, Object bean, Set<Method> nettyHandlers) {
    try {
      final SurfNettyListenerRegistry registry = surfNettyListenerRegistry.getObject();
      for (final Method handler : nettyHandlers) {
        registry.registerListener(handler, bean);
      }
    } catch (SurfNettyListenerRegistrationException e) {
      throw new BeanCreationException(beanName, e.getMessage(), e);
    }
  }
}
