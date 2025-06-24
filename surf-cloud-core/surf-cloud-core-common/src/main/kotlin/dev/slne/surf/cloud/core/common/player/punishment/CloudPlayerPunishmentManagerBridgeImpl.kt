package dev.slne.surf.cloud.core.common.player.punishment

import com.google.auto.service.AutoService
import com.google.common.flogger.StackSize
import dev.slne.surf.cloud.api.common.player.punishment.CloudPlayerPunishmentManagerBridge
import dev.slne.surf.cloud.api.common.player.punishment.PunishmentLoginValidation
import dev.slne.surf.cloud.api.common.player.punishment.type.ban.PunishmentBan
import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.api.common.util.mutableObjectSetOf
import dev.slne.surf.cloud.api.common.util.synchronize
import dev.slne.surf.cloud.core.common.messages.MessageManager
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.core.common.player.PunishmentManager
import dev.slne.surf.cloud.api.common.player.task.PrePlayerJoinTask
import dev.slne.surf.cloud.core.common.util.bean
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.toObjectList
import it.unimi.dsi.fastutil.objects.ObjectList
import org.jetbrains.annotations.Unmodifiable
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.SmartLifecycle
import org.springframework.core.OrderComparator
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@AutoService(CloudPlayerPunishmentManagerBridge::class)
class CloudPlayerPunishmentManagerBridgeImpl : CloudPlayerPunishmentManagerBridge {
    val loginValidations = mutableObjectListOf<PunishmentLoginValidation>().synchronize()

    override fun registerLoginValidation(check: PunishmentLoginValidation) {
        if (loginValidations.contains(check)) return
        loginValidations.add(check)
        OrderComparator.sort(loginValidations)
    }

    fun registerLoginValidations(loginValidations: Collection<PunishmentLoginValidation>) {
        for (validation in loginValidations) {
            if (!this.loginValidations.contains(validation)) {
                this.loginValidations.add(validation)
            }
        }
        OrderComparator.sort(this.loginValidations)
    }

    override fun unregisterLoginValidation(check: PunishmentLoginValidation) {
        loginValidations.remove(check)
    }

    override suspend fun fetchIpBans(
        ip: String,
        onlyActive: Boolean,
        sort: Boolean
    ): @Unmodifiable ObjectList<out PunishmentBan> {
        val fetched = bean<PunishmentManager>().fetchIpBans(ip, onlyActive)
        return (if (sort) fetched.sorted() else fetched).toObjectList()
    }

    @Component
    class LoginValidationAutoRegistrationHandler : BeanPostProcessor, SmartLifecycle {

        private val watched = mutableObjectSetOf<PunishmentLoginValidation>()
        private var running = false

        override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
            if (bean is PunishmentLoginValidation) {
                watched.add(bean)
                if (running) {
                    punishmentManagerBridgeImpl.registerLoginValidation(bean)
                }
            }

            return bean
        }

        override fun start() {
            punishmentManagerBridgeImpl.registerLoginValidations(watched)
            running = true
        }

        override fun stop() {
            running = false
            watched.forEach { punishmentManagerBridgeImpl.unregisterLoginValidation(it) }
        }

        override fun isRunning(): Boolean {
            return running
        }
    }

    @Component
    @Order(PrePlayerJoinTask.PUNISHMENT_LOGIN_VALIDATION_HANDLER)
    class LoginValidationHandler(private val punishmentManager: PunishmentManager) :
        PrePlayerJoinTask {
        private val log = logger()

        override suspend fun preJoin(player: OfflineCloudPlayer): PrePlayerJoinTask.Result {
            val cache = punishmentManager.getCurrentLoginValidationPunishmentCache(player.uuid)
            if (cache == null) {
                log.atWarning()
                    .withStackTrace(StackSize.SMALL)
                    .log("No Punishment cache found for player $player")
                return PrePlayerJoinTask.Result.ERROR
            }

            val longestBan = cache.bans.find { it.active }
            val banResult = if (longestBan != null) {
                PrePlayerJoinTask.Result.DENIED(
                    MessageManager.Punish.Ban(longestBan).banDisconnectComponent()
                )
            } else {
                PrePlayerJoinTask.Result.ALLOWED
            }

            val loginValidations = punishmentManagerBridgeImpl.loginValidations
            if (loginValidations.isEmpty()) {
                return banResult
            }

            for (validation in loginValidations) {
                val result = validation.performLoginCheck(player, cache)
                if (result !is PunishmentLoginValidation.Result.ALLOWED) {
                    return wrap(result)
                }
            }

            return banResult
        }

        private fun wrap(from: PunishmentLoginValidation.Result) = when (from) {
            PunishmentLoginValidation.Result.ALLOWED -> PrePlayerJoinTask.Result.ALLOWED
            is PunishmentLoginValidation.Result.DENIED -> PrePlayerJoinTask.Result.DENIED(from.reason)
            PunishmentLoginValidation.Result.ERROR -> PrePlayerJoinTask.Result.ERROR
        }
    }
}

val punishmentManagerBridgeImpl get() = CloudPlayerPunishmentManagerBridge.instance as CloudPlayerPunishmentManagerBridgeImpl