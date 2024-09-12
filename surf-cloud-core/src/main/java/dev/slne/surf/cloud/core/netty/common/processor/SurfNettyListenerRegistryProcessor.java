package dev.slne.surf.cloud.core.netty.common.processor;

import dev.slne.surf.cloud.api.meta.SurfNettyPacketHandler;
import dev.slne.surf.cloud.core.netty.common.SurfNettyListenerRegistry;
import java.lang.reflect.Method;
import java.util.Set;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Component
public class SurfNettyListenerRegistryProcessor implements BeanPostProcessor {

  private final SurfNettyListenerRegistry surfNettyListenerRegistry;

  public SurfNettyListenerRegistryProcessor(SurfNettyListenerRegistry surfNettyListenerRegistry) {
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

  }
}
