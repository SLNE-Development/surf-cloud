package dev.slne.surf.cloud.core.netty.common.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.flogger.Flogger;

@Flogger
public final class NettyConnectionRecoveryHandler implements ChannelFutureListener {

  private final AbstractNettyConnection<?, ?, ?> nettyConnection;
  private final long reconnectInterval;
  private final AtomicBoolean shouldDisconnect = new AtomicBoolean(false);

  public NettyConnectionRecoveryHandler(AbstractNettyConnection<?, ?, ?> nettyConnection, Duration reconnectInterval) {
    this.nettyConnection = nettyConnection;
    this.reconnectInterval = reconnectInterval.toMillis();
  }

  public void stopReconnection() {
    shouldDisconnect.set(true);
  }

  public void resumeReconnect() {
    shouldDisconnect.set(false);
  }

  @Override
  public void operationComplete(ChannelFuture future) throws Exception {
    final Channel channel = future.channel();

    log.atWarning()
        .log("Connection to %s has been lost, reconnecting in %sms...",
            channel.remoteAddress(),
            reconnectInterval
        );

    channel.disconnect().sync();
    attemptReconnect(channel.eventLoop());
  }

  public void attemptReconnect(EventLoop eventLoop) {
    eventLoop.schedule(() -> {
      log.atInfo()
          .log("Attempting to reconnect to %s...",
              eventLoop);

      if (shouldDisconnect.get()) {
        log.atInfo()
            .log("Reconnect request has been cancelled, aborting...");
        return;
      }

      try {
        nettyConnection.tryEstablishConnection0();
      } catch (Exception e) {
        log.atWarning()
            .withCause(e)
            .log("Failed to reconnect to %s, retrying in %sms...",
                eventLoop,
                reconnectInterval
            );
        attemptReconnect(eventLoop);
      }
    }, reconnectInterval, TimeUnit.MILLISECONDS);
  }
}
