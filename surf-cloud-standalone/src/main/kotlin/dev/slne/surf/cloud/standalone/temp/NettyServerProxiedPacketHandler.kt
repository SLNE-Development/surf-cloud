package dev.slne.surf.cloud.standalone.temp

//@Component
//@Profile("server")
//class NettyServerProxiedPacketHandler : ChannelInitializerModifier {
//    override fun modify(channel: Channel) {
//        channel.pipeline().addBefore("packetHandler", "proxiedPacketHandler", Handler())
//    }
//
//    private class Handler : ChannelDuplexHandler() {
//        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
//            if (msg is ProxiedNettyPacket) {
//                msg.target.sendPacket(msg)
//            } else {
//                super.channelRead(ctx, msg)
//            }
//        }
//    }
//}
