package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.PacketHandlerMode
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.api.common.player.task.PrePlayerJoinTask
import dev.slne.surf.cloud.core.common.netty.network.InternalNettyPacket
import java.util.*

@SurfNettyPacket(
    "cloud:clientbound:run_pre_player_join_tasks",
    PacketFlow.CLIENTBOUND,
    handlerMode = PacketHandlerMode.DEFAULT
)
class ClientboundRunPrePlayerJoinTasksPacket(val uuid: UUID) :
    RespondingNettyPacket<RunPrePlayerJoinTasksResultPacket>(),
    InternalNettyPacket<RunningClientPacketListener> {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.UUID_CODEC,
            ClientboundRunPrePlayerJoinTasksPacket::uuid,
            ::ClientboundRunPrePlayerJoinTasksPacket
        )
    }

    override fun handle(listener: RunningClientPacketListener) {
        listener.handleRunPlayerPreJoinTasks(this)
    }
}

@SurfNettyPacket("cloud:response:run_pre_player_join_tasks", PacketFlow.SERVERBOUND)
class RunPrePlayerJoinTasksResultPacket(val result: PrePlayerJoinTask.Result) :
    ResponseNettyPacket() {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            PrePlayerJoinTask.Result.STREAM_CODEC,
            RunPrePlayerJoinTasksResultPacket::result,
            ::RunPrePlayerJoinTasksResultPacket
        )
    }
}