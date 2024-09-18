package dev.slne.surf.cloud.api.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jetbrains.annotations.ApiStatus.Internal;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SurfNettyPacket {

  int id();

  @Internal // only for reference public
  interface DefaultIds {
    int PROXIED_NETTY_PACKET = 0x00;
    int CLOUD_REGISTER_SERVER_PACKET = 0x01;
    int CLOUD_SERVER_INFO_PACKET = 0x02;
    int CLOUD_SERVER_INFO_BATCH_PACKET = 0x03;
    int CONTAINER_POST_CONNECTED = 0x04;
    int CLIENT_JOIN = 0x05;
    int CLIENT_QUIT = 0x06;
  }
}
