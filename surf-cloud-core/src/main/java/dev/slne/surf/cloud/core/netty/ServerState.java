package dev.slne.surf.cloud.core.netty;

public enum ServerState {
  RESTARTING(false),
  LOBBY(true),
  OFFLINE(false),
  ONLINE(true);

  private final boolean allowJoin;

  private ServerState(boolean allowJoin) {
    this.allowJoin = allowJoin;
  }

  public boolean allowJoin() {
    return allowJoin;
  }
}
