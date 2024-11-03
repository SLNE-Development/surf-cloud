package dev.slne.surf.cloud.core.netty.protocol.channel

//@Component
//class SurfNettyChannelInitializer(
//    private val modifiers: ObjectProvider<ChannelInitializerModifier>,
//    private val context: ConfigurableApplicationContext
//) {
//    private val log = logger()
//
//    fun initChannel(channel: Channel) {
//        val base = context.getBean(AbstractNettyBase::class.java)
//
//        with(channel.pipeline()) {
//            addLast("frameDecoder", LengthFieldBasedFrameDecoder(8192, 0, 4, 0, 4))
//            addLast("frameEncoder", LengthFieldPrepender(4, false))
//            addLast("decoder", NettyPacketDecoder())
//            addLast("encoder", NettyPacketEncoder())
//            addLast("commonJoinQuitHandler", NettyPacketJoinQuitCommonHandler(base.connection))
//            addLast("packetHandler", NettyPacketHandler(base))
//            addLast("logger", LoggingHandler(LogLevel.INFO))
//            addLast("exceptionHandler", object : ChannelDuplexHandler() {
//                override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
//                    log.atSevere()
//                        .withCause(cause)
//                        .log("Exception caught in channel %s", ctx.channel())
//                }
//            })
//        }
//
//        for (modifier in modifiers.orderedStream().toList()) {
//            modifier.modify(channel)
//        }
//    }
//}
