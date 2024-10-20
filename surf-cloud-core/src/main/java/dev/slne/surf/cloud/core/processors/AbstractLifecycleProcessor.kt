package dev.slne.surf.cloud.core.processors;

import dev.slne.surf.cloud.api.lifecycle.SurfLifecycle;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.flogger.Flogger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

@Flogger
public abstract class AbstractLifecycleProcessor implements BeanPostProcessor {

  private final MultiValueMap<Class<?>, SurfLifecycle> lifecycleMap = CollectionUtils.toMultiValueMap(
      new ConcurrentHashMap<>());

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (!(bean instanceof SurfLifecycle lifecycle)) {
      return bean;
    }

    final Class<?> providingClass = getProvidingClass(lifecycle);
    lifecycleMap.add(providingClass, lifecycle);

    return bean;
  }

  protected abstract Class<?> getProvidingClass(SurfLifecycle lifecycle);

  public final List<SurfLifecycle> getLifecycles(Class<?> providingClass) {
    return lifecycleMap.get(providingClass);
  }
}
