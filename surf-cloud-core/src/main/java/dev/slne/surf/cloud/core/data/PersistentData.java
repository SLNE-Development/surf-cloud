package dev.slne.surf.cloud.core.data;

import java.util.function.Function;
import net.querz.nbt.tag.Tag;
import org.jetbrains.annotations.Nullable;

public interface PersistentData<T> {

  T value();

  void setValue(T value);

  static <T extends Tag<D>, D> PersistentData<D> data(String key, Class<T> type,
      Function<T, D> toValue, Function<D, T> toTag) {
    return data(key, type, toValue, toTag, null);
  }

  static <T extends Tag<D>, D> PersistentData<D> data(String key, Class<T> type,
      Function<T, D> toValue, Function<D, T> toTag, @Nullable D defaultValue) {
    return PersistentDataImpl.data(key, type, toValue, toTag, defaultValue);
  }
}
