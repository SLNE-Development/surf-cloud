package dev.slne.surf.cloud.core.data;

import dev.slne.surf.cloud.core.SurfCloudCoreInstance;
import java.io.File;
import java.util.function.Function;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.Tag;
import org.jetbrains.annotations.Nullable;

@UtilityClass
class PersistentDataImpl {

  private CompoundTag tag;

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @SneakyThrows
  private File getFile() {
    final File file = SurfCloudCoreInstance.get().getDataFolder().resolve("data.dat").toFile();

    if (!file.exists()) {
      file.createNewFile();
    }

    return file;
  }

  @SneakyThrows
  private CompoundTag getOrLoadTag() {
    if (tag == null) {
      tag = (CompoundTag) NBTUtil.read(getFile(), true).getTag();
    }

    return tag;
  }

  @SneakyThrows
  private void saveTag() {
    NBTUtil.write(tag, getFile(), true);
  }

  <T extends Tag<D>, D> PersistentData<D> data(String key, Class<T> type, Function<T, D> toValue,
      Function<D, T> toTag, @Nullable D defaultValue) {
    return new DataImpl<>(getOrLoadTag(), key, toValue, toTag, type, defaultValue);
  }


  private record DataImpl<T extends Tag<D>, D>(CompoundTag tag,
                                               String key,
                                               Function<T, D> toValue,
                                               Function<D, T> toTag,
                                               Class<T> type,
                                               @Nullable D defaultValue) implements
      PersistentData<D> {

    @Override
    public @Nullable D value() {
      final T t = tag.get(key, type);

      if (t == null) {
        return defaultValue;
      }

      return toValue.apply(t);
    }

    @Override
    public void setValue(D value) {
      if (value == null) {
        tag.remove(key);
        return;
      }

      tag.put(key, toTag.apply(value));
    }
  }
}
