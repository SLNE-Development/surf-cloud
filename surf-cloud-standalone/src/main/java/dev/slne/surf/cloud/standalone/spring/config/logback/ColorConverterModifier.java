package dev.slne.surf.cloud.standalone.spring.config.logback;

import ch.qos.logback.classic.Level;
import dev.slne.surf.surfapi.core.api.util.Util;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiElement;
import org.springframework.boot.logging.logback.ColorConverter;

@UtilityClass
class ColorConverterModifier {

  private void modify(@NotNull Map<Integer, AnsiElement> modifiableLevels) {
    modifiableLevels.put(Level.INFO_INTEGER, AnsiColor.DEFAULT);
  }

  public void changeInfoColorToWhite() throws Exception {
    final Field levelsField = ColorConverter.class.getDeclaredField("LEVELS");
    levelsField.setAccessible(true);

    final Map<Integer, AnsiElement> modifiableLevels = extractLevels(levelsField);
    modify(modifiableLevels);

    Util.setStaticFinalField(levelsField, Collections.unmodifiableMap(modifiableLevels));
  }

  @Contract("_ -> new")
  @SuppressWarnings("unchecked")
  private @NotNull Map<Integer, AnsiElement> extractLevels(@NotNull Field field) throws Exception {
    return new HashMap<>((Map<Integer, AnsiElement>) field.get(null));
  }
}
