package dev.slne.surf.cloud.core.common.netty.network.protocol.running

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.cloud.core.common.player.task.PrePlayerJoinTask
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@SurfNettyPacket("cloud:clientbound:run_pre_player_join_tasks", PacketFlow.CLIENTBOUND)
@Serializable
class ClientboundRunPrePlayerJoinTasksPacket(val uuid: @Contextual UUID) :
    RespondingNettyPacket<RunPrePlayerJoinTasksResultPacket>()

@SurfNettyPacket("cloud:response:run_pre_player_join_tasks", PacketFlow.SERVERBOUND)
@Serializable
class RunPrePlayerJoinTasksResultPacket(val result: PrePlayerJoinTask.Result) : ResponseNettyPacket()