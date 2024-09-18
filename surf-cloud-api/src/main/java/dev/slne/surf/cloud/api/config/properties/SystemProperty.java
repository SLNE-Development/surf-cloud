package dev.slne.surf.cloud.api.config.properties;

import java.util.function.Function;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.Nullable;

@NonExtendable
public interface SystemProperty<T> {

  T value();

  static <T> SystemProperty<T> property(
      String key,
      Function<String, T> parser,
      @Nullable T defaultValue
  ) {
    return property(String.join(".", "surf", "cloud"), key, parser, defaultValue);
  }

  static <T> SystemProperty<T> property(
      String prefix,
      String key,
      Function<String, T> parser,
      @Nullable T defaultValue
  ) {
    return SystemPropertyImpl.property(prefix + "." + key, parser, defaultValue);
  }
}
