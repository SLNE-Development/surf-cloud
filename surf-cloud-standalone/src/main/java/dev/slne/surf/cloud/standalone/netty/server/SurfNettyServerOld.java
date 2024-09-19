package dev.slne.surf.cloud.standalone.netty.server;

//@Profile("independent")
//@Component
public class SurfNettyServerOld
//    extends NettyBase
{

//  private final ServerBootstrap serverBootstrap;
//  private final SurfNettyChannelInitializer surfNettyChannelInitializer;
//  private EventLoopGroup parentGroup = new NioEventLoopGroup();
//  private EventLoopGroup workerGroup = new NioEventLoopGroup();
//  private SocketChannel connectedChannel;
//
//  public SurfNettyServerOld(SurfNettyChannelInitializer surfNettyChannelInitializer) {
//    super(name);
//
//    this.serverBootstrap = new ServerBootstrap()
//        .option(ChannelOption.AUTO_READ, true)
//        .option(ChannelOption.SO_KEEPALIVE, true)
//        .group(parentGroup, workerGroup)
//        .childHandler(surfNettyChannelInitializer)
//        .channel(NioServerSocketChannel.class);
//    this.surfNettyChannelInitializer = surfNettyChannelInitializer;
//  }
//
//  @PostConstruct
//  public void start() {
//    serverBootstrap.bind(new InetSocketAddress("127.0.0.1", 8888))
//        .syncUninterruptibly();
//  }
//
//
//  @Override
//  protected NettyContainer<?, ?, ?> createContainer() {
//    return null;
//  }
//
//  @PreDestroy
//  public void close() {
//    parentGroup.shutdownGracefully().syncUninterruptibly();
//    workerGroup.shutdownGracefully().syncUninterruptibly();
//  }
}
