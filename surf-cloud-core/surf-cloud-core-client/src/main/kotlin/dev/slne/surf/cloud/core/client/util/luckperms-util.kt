package dev.slne.surf.cloud.core.client.util

import kotlinx.coroutines.future.await
import net.luckperms.api.LuckPermsProvider
import net.luckperms.api.model.user.User
import net.luckperms.api.model.user.UserManager
import java.util.UUID

val luckperms get() = LuckPermsProvider.get()

suspend fun UserManager.getOrLoadUser(uuid: UUID): User = getUser(uuid) ?: loadUser(uuid).await()