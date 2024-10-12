package dev.slne.surf.cloud.api.netty.source.tracker

import dev.slne.surf.cloud.api.netty.annotation.PlatformRequirement
import dev.slne.surf.cloud.api.netty.source.ProxiedNettySource
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Unmodifiable
import org.jetbrains.annotations.UnmodifiableView

@ApiStatus.NonExtendable
@PlatformRequirement(PlatformRequirement.Platform.COMMON)
interface NettyClientTracker<Client : ProxiedNettySource<Client>> {
    fun findByGroupId(groupId: String): @Unmodifiable ObjectSet<Client>

    fun findByServerGuid(serverGuid: Long): Client?

    val clients: @UnmodifiableView ObjectSet<Client>?
}
