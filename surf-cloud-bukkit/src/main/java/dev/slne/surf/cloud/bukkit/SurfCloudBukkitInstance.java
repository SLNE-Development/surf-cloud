package dev.slne.surf.cloud.bukkit;

import com.google.auto.service.AutoService;
import dev.slne.surf.cloud.api.SurfCloudInstance;
import dev.slne.surf.cloud.core.SurfCloudCoreInstance;
import java.nio.file.Path;

@AutoService(SurfCloudInstance.class)
public final class SurfCloudBukkitInstance extends SurfCloudCoreInstance {

  @Override
  public Path getDataFolder() {
    return BukkitMain.getInstance().getDataPath();
  }

  @Override
  public ClassLoader getClassLoader() {
    return BukkitMain.getInstance().getClassLoader0();
  }
}
