package dev.slne.surf.cloud.velocity.processor;

import com.velocitypowered.api.event.Subscribe;
import dev.slne.surf.cloud.velocity.VelocityMain;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Component
public class VelocityListenerProcessor implements BeanPostProcessor {

  @Override
  public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName)
      throws BeansException {
    if (bean instanceof AopInfrastructureBean) {
      return bean;
    }

    final Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);

    if (AnnotationUtils.isCandidateClass(targetClass, Subscribe.class)) {
      registerEventHandlers(beanName, bean);
    }

    return bean;
  }

  private void registerEventHandlers(String beanName, Object bean) throws BeansException {
    try {
      VelocityMain.getInstance().getEventManager().register(VelocityMain.getInstance(), bean);
    } catch (Throwable e) {
      throw new BeanCreationException(beanName, "Failed to register event handler methods", e);
    }
  }
}
