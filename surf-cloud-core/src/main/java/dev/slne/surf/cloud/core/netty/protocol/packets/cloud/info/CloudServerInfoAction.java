package dev.slne.surf.cloud.core.netty.protocol.packets.cloud.info;

import dev.slne.surf.cloud.api.server.CloudServer;

public enum CloudServerInfoAction {

  /**
   * Updates properties of a {@link CloudServer}
   */
  UPDATE_SERVER_INFO,
  /**
   * Removes a {@link CloudServer}
   */
  REMOVE_SERVER_INFO,
  /**
   * Adds a {@link CloudServer}
   */
  ADD_SERVER_INFO
}
