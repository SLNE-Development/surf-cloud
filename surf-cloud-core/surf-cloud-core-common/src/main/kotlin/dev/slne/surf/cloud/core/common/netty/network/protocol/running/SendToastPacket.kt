package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.common.player.toast.NetworkToast
import java.util.*

@SurfNettyPacket("cloud:bidirectional:send_toast", PacketFlow.BIDIRECTIONAL)
class SendToastPacket(
    val uuid: UUID,
    val toast: NetworkToast,
) : NettyPacket() {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            SendToastPacket::uuid,
            NetworkToast.STREAM_CODEC,
            SendToastPacket::toast,
            ::SendToastPacket
        )
    }
}