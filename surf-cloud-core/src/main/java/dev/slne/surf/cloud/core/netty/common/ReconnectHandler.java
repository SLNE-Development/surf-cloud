package dev.slne.surf.cloud.core.netty.common;

import dev.slne.surf.cloud.core.netty.NettyContainer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.flogger.Flogger;

@Flogger
public class ReconnectHandler implements ChannelFutureListener {

  private final NettyContainer<?> container;
  private final Duration reconnectDelay;
  private final AtomicBoolean disconnectRequested = new AtomicBoolean(false);

  public ReconnectHandler(NettyContainer<?> container, Duration reconnectDelay) {
    this.container = container;
    this.reconnectDelay = reconnectDelay;
  }

  public void requestDisconnect() {
    disconnectRequested.set(true);
  }

  public void requestReconnect() {
    disconnectRequested.set(false);
  }

  @Override
  public void operationComplete(ChannelFuture future) throws Exception {
    final Channel channel = future.channel();

    log.atWarning()
        .log("Connection to %s has been lost, reconnecting in %sms...",
            channel.remoteAddress(),
            reconnectDelay.toMillis()
        );

    channel.disconnect();
    scheduleReconnect(channel.eventLoop());
  }

  public void scheduleReconnect(EventLoop eventLoop) {
    eventLoop.schedule(() -> {
      log.atInfo()
          .log("Attempting to reconnect to %s...",
              eventLoop);

      if (disconnectRequested.get()) {
        log.atInfo()
            .log("Reconnect request has been cancelled, aborting...");
        return;
      }

      container.start();
    }, reconnectDelay.toMillis(), TimeUnit.MILLISECONDS);
  }
}
