package dev.slne.surf.cloud.api.util;

import java.util.concurrent.CompletableFuture;

public interface AdvancedAutoCloseable extends AutoCloseable {

  CompletableFuture<Void> start();
}
