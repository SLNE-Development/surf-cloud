package dev.slne.surf.cloud.core.client.player

import dev.slne.surf.cloud.api.client.netty.packet.awaitOrThrow
import dev.slne.surf.cloud.api.client.netty.packet.fireAndAwaitOrThrow
import dev.slne.surf.cloud.api.common.player.whitelist.MutableWhitelistEntry
import dev.slne.surf.cloud.api.common.util.Either
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundCreateWhitelistPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundRequestWhitelistPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundRequestWhitelistStatusPacket
import dev.slne.surf.cloud.core.common.netty.network.protocol.running.ServerboundUpdateWhitelistPacket
import dev.slne.surf.cloud.core.common.player.whitelist.MutableWhitelistEntryImpl
import dev.slne.surf.cloud.core.common.player.whitelist.WhitelistEntryImpl
import dev.slne.surf.cloud.core.common.player.whitelist.WhitelistService
import org.springframework.stereotype.Component
import java.util.*

@Component
class WhitelistServiceImpl : WhitelistService {

    override suspend fun whitelistStatus(
        uuid: UUID,
        groupOrServer: Either<String, String>
    ) = ServerboundRequestWhitelistStatusPacket(uuid, groupOrServer)
        .fireAndAwaitOrThrow()
        .status

    override suspend fun getWhitelist(
        uuid: UUID,
        groupOrServer: Either<String, String>
    ) = ServerboundRequestWhitelistPacket(uuid, groupOrServer)
        .fireAndAwaitOrThrow()
        .whitelist


    override suspend fun createWhitelist(whitelist: WhitelistEntryImpl) =
        ServerboundCreateWhitelistPacket(whitelist)
            .fireAndAwaitOrThrow()
            .whitelist

    override suspend fun editWhitelist(
        uuid: UUID,
        groupOrServer: Either<String, String>,
        edit: MutableWhitelistEntry.() -> Unit
    ): Boolean {
        val existing = getWhitelist(uuid, groupOrServer) ?: return false
        val mutableExisting = existing.toMutableWhitelistEntry()
        val edited = mutableExisting.copy()
        edited.edit()

        if (edited == mutableExisting) {
            return false // No changes made
        }

        return updateWhitelist(edited)
    }

    override suspend fun updateWhitelist(updated: MutableWhitelistEntryImpl) =
        ServerboundUpdateWhitelistPacket(updated)
            .awaitOrThrow()
}