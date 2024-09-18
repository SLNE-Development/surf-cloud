package dev.slne.surf.cloud.standalone;

import com.google.auto.service.AutoService;
import dev.slne.surf.cloud.api.SurfCloudInstance;
import dev.slne.surf.cloud.core.SurfCloudCoreInstance;
import java.nio.file.Path;

@AutoService(SurfCloudInstance.class)
public class SurfCloudStandaloneInstance extends SurfCloudCoreInstance {

  @Override
  public Path getDataFolder() {
    return Path.of("");
  }

  @Override
  public ClassLoader getClassLoader() {
    return getClass().getClassLoader();
  }

  @Override
  protected String getSpringProfile() {
    return "independent";
  }
}
