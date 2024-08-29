package dev.slne.surf.data.api;

import net.kyori.adventure.util.Services;

final class SurfDataInstanceAccessor {

  private static final SurfDataInstance INSTANCE = Services.service(SurfDataInstance.class)
      .orElseThrow(() -> new Error("SurfDataInstance not available"));

  static SurfDataInstance get() {
    return INSTANCE;
  }
}
