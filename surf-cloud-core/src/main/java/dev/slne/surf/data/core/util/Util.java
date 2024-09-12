package dev.slne.surf.data.core.util;

import com.google.common.flogger.FluentLogger;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Util {

  public void tempChangeSystemClassLoader(ClassLoader classLoader, Runnable runnable) {
    final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(classLoader);
    runnable.run();
    Thread.currentThread().setContextClassLoader(originalClassLoader);
  }

  public <T> T tempChangeSystemClassLoader(ClassLoader classLoader, Supplier<T> supplier) {
    final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(classLoader);
    T result = supplier.get();
    Thread.currentThread().setContextClassLoader(originalClassLoader);

    return result;
  }

  public void measureTime(FluentLogger logger, Long2ObjectFunction<String> messageSupplier,
      Runnable runnable) {
    final long start = System.currentTimeMillis();
    runnable.run();
    final long end = System.currentTimeMillis();
    logger.atInfo().log(messageSupplier.apply(end - start));
  }
}
