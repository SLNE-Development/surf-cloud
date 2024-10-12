package dev.slne.surf.cloud.api.meta

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SurfNettyPacket(val id: Int)

internal object DefaultIds {
    const val PROXIED_NETTY_PACKET = 0x00
    const val KEEP_ALIVE_PACKET = 0x01
    const val CLOUD_REGISTER_SERVER_PACKET = 0x02
    const val CLOUD_SERVER_INFO_PACKET = 0x03
    const val CLOUD_SERVER_INFO_BATCH_PACKET = 0x04
    const val CONTAINER_POST_CONNECTED = 0x05
    const val CLIENT_JOIN = 0x06
    const val CLIENT_QUIT = 0x07
}
