package dev.slne.surf.cloud.api;

import net.kyori.adventure.util.Services;

final class SurfCloudInstanceAccessor {

  private static final SurfCloudInstance INSTANCE = Services.service(SurfCloudInstance.class)
      .orElseThrow(() -> new Error("SurfCloudInstance not available"));

  static SurfCloudInstance get() {
//    AbstractPersistable // TODO: Implement this
//    AbstractAuditable

    return INSTANCE;
  }
}
