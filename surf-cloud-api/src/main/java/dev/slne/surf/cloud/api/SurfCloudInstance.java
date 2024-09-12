package dev.slne.surf.cloud.api;

import dev.slne.surf.cloud.api.redis.RedisEvent;
import javax.annotation.ParametersAreNonnullByDefault;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Contract;
import org.springframework.context.ConfigurableApplicationContext;

@NonExtendable
@ParametersAreNonnullByDefault
public interface SurfCloudInstance {

  default ConfigurableApplicationContext startSpringApplication(Class<?> applicationClass) {
    return startSpringApplication(applicationClass, applicationClass.getClassLoader());
  }

  ConfigurableApplicationContext startSpringApplication(
      Class<?> applicationClass,
      ClassLoader classLoader,
      ClassLoader... parentClassLoader
  );

  @Internal
  ClassLoader getClassLoader();

  void callRedisEvent(RedisEvent event);

  @Contract(pure = true)
  static SurfCloudInstance get() {
    return SurfCloudInstanceAccessor.get();
  }
}
