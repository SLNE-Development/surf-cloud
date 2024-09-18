package dev.slne.surf.cloud.api.config.properties;

import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

@UtilityClass
class SystemPropertyImpl {

  <T> PropertyImpl<T> property(
      String key,
      Function<String, T> parser,
      @Nullable T defaultValue
  ) {
    return new PropertyImpl<>(key, parser, defaultValue);
  }

  @RequiredArgsConstructor
  private static final class PropertyImpl<T> implements SystemProperty<T> {

    private final String key;
    private final Function<String, T> parser;
    private final T defaultValue;
    private @Nullable T value;
    private boolean initialized;

    @Override
    public T value() {
      if (!initialized) {
        final String property = System.getProperty(key);
        this.value = property != null ? parser.apply(property) : defaultValue;
        this.initialized = true;
      }

      return this.value;
    }
  }
}
