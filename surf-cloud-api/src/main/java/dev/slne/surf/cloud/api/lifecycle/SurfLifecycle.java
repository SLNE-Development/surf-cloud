package dev.slne.surf.cloud.api.lifecycle;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.springframework.stereotype.Component;

@OverrideOnly
@Component
public interface SurfLifecycle {

  default void onLoad() {

  }

  default void onEnable() {

  }

  default void onDisable() {

  }

  default void onReload() {
    onDisable();
    onEnable();
  }
}
