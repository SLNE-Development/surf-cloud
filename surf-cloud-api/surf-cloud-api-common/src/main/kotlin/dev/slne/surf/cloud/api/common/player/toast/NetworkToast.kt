package dev.slne.surf.cloud.api.common.player.toast

import dev.slne.surf.cloud.api.common.netty.network.codec.ByteBufCodecs
import dev.slne.surf.cloud.api.common.netty.network.codec.StreamCodec
import dev.slne.surf.cloud.api.common.netty.network.codec.composite
import dev.slne.surf.cloud.api.common.util.ByIdMap
import dev.slne.surf.cloud.api.common.util.IdRepresentable
import dev.slne.surf.surfapi.core.api.messages.builder.SurfComponentBuilder
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component

data class NetworkToast(
    val icon: Key,
    val title: Component,
    val frame: Frame = Frame.TASK,
) {
    companion object {
        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.KEY_CODEC,
            NetworkToast::icon,
            ByteBufCodecs.COMPONENT_CODEC,
            NetworkToast::title,
            Frame.STREAM_CODEC,
            NetworkToast::frame,
            ::NetworkToast,
        )

        inline fun create(block: Builder.() -> Unit) = Builder().apply(block).build()
        inline operator fun invoke(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    class Builder @PublishedApi internal constructor() {
        private var icon: Key? = null
        private var title: Component? = null
        private var frame: Frame = Frame.TASK

        fun icon(icon: Key) {
            this.icon = icon
        }

        fun title(title: Component) {
            this.title = title
        }

        fun title(title: SurfComponentBuilder.() -> Unit) = title(SurfComponentBuilder(title))

        fun frame(frame: Frame) {
            this.frame = frame
        }

        fun build() = NetworkToast(
            icon = icon ?: error("Icon is required"),
            title = title ?: error("Title is required"),
            frame = frame,
        )
    }

    enum class Frame(override val id: Int) : IdRepresentable {
        TASK(0),
        CHALLENGE(1),
        GOAL(2);

        companion object {
            val BY_ID = IdRepresentable.enumIdMap<Frame>(ByIdMap.OutOfBoundsStrategy.ZERO)
            val STREAM_CODEC = IdRepresentable.codec(BY_ID)
        }
    }
}