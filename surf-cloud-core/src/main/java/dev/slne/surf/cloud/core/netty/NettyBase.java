package dev.slne.surf.cloud.core.netty;

import java.io.Closeable;
import java.io.IOException;

public abstract class NettyBase implements Closeable {

  private final String name;
  private int port = 5555;
  private String host = "127.0.0.1";
  private int reconnectDelay = 3;

  public NettyBase(String name) {
    this.name = name;
  }

  @Override
  public void close() throws IOException {
    // close
  }
}
