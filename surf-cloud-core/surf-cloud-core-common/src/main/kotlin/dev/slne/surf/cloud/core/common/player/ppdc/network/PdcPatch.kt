package dev.slne.surf.cloud.core.common.player.ppdc.network

import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs

data class PdcPatch(val ops: MutableList<PdcOp>) {
    val empty get() = ops.isEmpty()

    companion object {
        val STREAM_CODEC = PdcOp.STREAM_CODEC
            .apply(ByteBufCodecs.list())
            .map(::PdcPatch, PdcPatch::ops)
    }
}