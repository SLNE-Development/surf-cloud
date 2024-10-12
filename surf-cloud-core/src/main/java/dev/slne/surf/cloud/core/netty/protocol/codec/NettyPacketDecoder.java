package dev.slne.surf.cloud.core.netty.protocol.codec;

import com.google.common.flogger.StackSize;
import dev.slne.surf.cloud.api.netty.packet.NettyPacket;
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf;
import dev.slne.surf.cloud.core.netty.common.registry.packet.NettyPacketRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.flogger.Flogger;

@Flogger
public class NettyPacketDecoder extends MessageToMessageDecoder<ByteBuf> {

  private final NettyPacketRegistry packetRegistry;

  public NettyPacketDecoder(NettyPacketRegistry packetRegistry) {
    this.packetRegistry = packetRegistry;
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
    if (!msg.isReadable(4)) {
      return;
    }

    final int packetId = msg.readInt();
    final NettyPacket<?> packet = packetRegistry.createPacket(packetId);

    if (packet == null) {
      log.atWarning()
          .withStackTrace(StackSize.MEDIUM)
          .atMostEvery(5, TimeUnit.SECONDS)
          .log("Received unknown packet with id %d", packetId);
      return;
    }

    final SurfByteBuf surfByteBuf = new SurfByteBuf(msg);
    final NettyPacket<?> decoded = packet.decode(surfByteBuf);

    if (decoded != null) {
      out.add(decoded);
    }
  }
}
