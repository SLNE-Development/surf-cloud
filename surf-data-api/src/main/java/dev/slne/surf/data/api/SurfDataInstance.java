package dev.slne.surf.data.api;

import javax.annotation.ParametersAreNonnullByDefault;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Contract;
import org.springframework.context.ConfigurableApplicationContext;

@NonExtendable
@ParametersAreNonnullByDefault
public interface SurfDataInstance {

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

  @Contract(pure = true)
  static SurfDataInstance get() {
    return SurfDataInstanceAccessor.get();
  }
}
