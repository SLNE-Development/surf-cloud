package dev.slne.surf.cloud.test.core

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.client.netty.packet.fireAndAwait
import dev.slne.surf.cloud.api.common.util.objectSetOf
import dev.slne.surf.cloud.test.api.FindAllTestModelsPacket
import dev.slne.surf.cloud.test.api.FindTestModelPacket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kyori.adventure.util.Services.Fallback
import org.springframework.stereotype.Service

@Service
@AutoService(TestModelService::class)
object CoreTestModelService : TestModelService, Fallback {

    override suspend fun findAll() = withContext(Dispatchers.IO) {
        println("Using fallback to find all")

        FindAllTestModelsPacket().fireAndAwait()?.testModels ?: objectSetOf()
    }

    override suspend fun findByName(name: String) = withContext(Dispatchers.IO) {
        println("Using fallback to find by name")

        FindTestModelPacket(name).fireAndAwait()?.testModel
    }
}