package dev.slne.surf.data.core.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Util {

  public void tempChangeSystemClassLoader(ClassLoader classLoader, Runnable runnable) {
    final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(classLoader);
    runnable.run();
    Thread.currentThread().setContextClassLoader(originalClassLoader);
  }
}
