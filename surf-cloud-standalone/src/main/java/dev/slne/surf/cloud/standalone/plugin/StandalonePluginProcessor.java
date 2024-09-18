package dev.slne.surf.cloud.standalone.plugin;

import org.jetbrains.annotations.NotNull;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Component
public class StandalonePluginProcessor implements BeanPostProcessor {

  private final StandalonePluginManager standalonePluginManager;

  public StandalonePluginProcessor(StandalonePluginManager standalonePluginManager) {
    this.standalonePluginManager = standalonePluginManager;
  }

  @Override
  public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
    if (bean instanceof AopInfrastructureBean) {
      return bean;
    }

    final Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
    if (AnnotationUtils.findAnnotation(targetClass, StandalonePluginMeta.class) == null) {
      return bean;
    }

    if (!(bean instanceof StandalonePlugin plugin)) {
      throw new BeanCreationException(beanName, "Bean annotated with @StandalonePluginMeta must extend StandalonePlugin");
    }

    plugin.start();
    standalonePluginManager.addPlugin(plugin);
    return bean;
  }
}
