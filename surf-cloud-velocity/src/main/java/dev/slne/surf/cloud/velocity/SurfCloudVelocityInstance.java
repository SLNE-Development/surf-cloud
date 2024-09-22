package dev.slne.surf.cloud.velocity;

import com.google.auto.service.AutoService;
import dev.slne.surf.cloud.api.SurfCloudInstance;
import dev.slne.surf.cloud.core.SurfCloudCoreInstance;
import java.nio.file.Path;

@AutoService(SurfCloudInstance.class)
public final class SurfCloudVelocityInstance extends SurfCloudCoreInstance {

  public SurfCloudVelocityInstance() throws IllegalAccessException {
  }

  @Override
  public Path getDataFolder() {
    return null;
  }

  @Override
  public ClassLoader getClassLoader() {
    return null;
  }
}
