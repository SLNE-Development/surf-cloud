package dev.slne.surf.cloud.core.util;

import com.google.common.flogger.FluentLogger;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;
import lombok.extern.flogger.Flogger;

@UtilityClass
@Flogger
public class Util {

  private final SecureRandom RANDOM;

  static {
    SecureRandom temp;
    try {
      temp = SecureRandom.getInstanceStrong();
    } catch (NoSuchAlgorithmException e) {
      log.atWarning()
          .withCause(e)
          .log("Failed to initialize secure random instance, falling back to default");

      temp = new SecureRandom();
    }
    RANDOM = temp;
  }

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

  public SecureRandom getRandom() {
    return RANDOM;
  }

  public static Class<?> getCallerClass() {
    class StackWalkerHolder {
      static final StackWalker INSTANCE = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    }

    return StackWalkerHolder.INSTANCE.walk(frames -> frames.skip(3).findFirst().map(StackWalker.StackFrame::getDeclaringClass).orElse(null));
  }
}
