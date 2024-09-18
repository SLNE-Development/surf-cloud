package dev.slne.surf.cloud.standalone.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.Pattern;
import org.springframework.stereotype.Component;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface StandalonePluginMeta {

  @PluginIdPattern String id();

  @Pattern(PluginIdPattern.ID_PATTERN)
  @interface PluginIdPattern {
    @Language("RegExp") String ID_PATTERN = "[a-z0-9_-]+";
  }
}
