package dev.slne.surf.cloud.core.netty.common.source;

import dev.slne.surf.cloud.api.netty.source.NettyServerSource;
import dev.slne.surf.cloud.core.netty.AbstractNettyBase;

public class NettyServerSourceImpl extends AbstractProxiedNettySource<NettyServerSource> implements
    NettyServerSource {

  public NettyServerSourceImpl(AbstractNettyBase<?, ?, NettyServerSource> nettyBase) {
    super(nettyBase);
  }
}
