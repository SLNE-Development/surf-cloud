package dev.slne.surf.cloud.core.common.player.task

import dev.slne.surf.cloud.api.common.util.mutableObjectListOf
import dev.slne.surf.cloud.core.common.coroutines.PrePlayerJoinTaskScope
import dev.slne.surf.cloud.core.common.player.CommonCloudPlayerImpl
import dev.slne.surf.cloud.core.common.player.CommonOfflineCloudPlayerImpl
import dev.slne.surf.surfapi.core.api.util.logger
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.core.OrderComparator
import org.springframework.stereotype.Component

@Component
class PrePlayerJoinTaskManager : BeanPostProcessor, SmartInitializingSingleton {

    private val log = logger()

    val tasks = mutableObjectListOf<PrePlayerJoinTask>()

    override fun postProcessAfterInitialization(
        bean: Any,
        beanName: String
    ): Any {
        if (bean is PrePlayerJoinTask) {
            tasks.add(bean)
        }

        return bean
    }

    override fun afterSingletonsInstantiated() {
        OrderComparator.sort(tasks)
    }

    suspend fun runTasks(player: CommonOfflineCloudPlayerImpl): PrePlayerJoinTask.Result =
        withContext(PrePlayerJoinTaskScope.context) {
            for (task in tasks) {
                val result = try {
                    task.preJoin(player)
                } catch (e: Exception) {
                    log.atSevere()
                        .withCause(e)
                        .log("Failed to run pre player join tasks")
                    return@withContext PrePlayerJoinTask.Result.ERROR
                }

                when (result) {
                    is PrePlayerJoinTask.Result.ALLOWED -> continue
                    else -> return@withContext result
                }
            }

            PrePlayerJoinTask.Result.ALLOWED
        }
}