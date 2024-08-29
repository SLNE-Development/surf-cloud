package dev.slne.surf.data.velocity;

import com.google.auto.service.AutoService;
import dev.slne.surf.data.api.SurfDataInstance;
import dev.slne.surf.data.core.SurfDataCoreInstance;
import java.nio.file.Path;

@AutoService(SurfDataInstance.class)
public final class SurfDataVelocityInstance extends SurfDataCoreInstance {

  @Override
  public Path getDataFolder() {
    return null;
  }

  @Override
  public ClassLoader getClassLoader() {
    return null;
  }
}
