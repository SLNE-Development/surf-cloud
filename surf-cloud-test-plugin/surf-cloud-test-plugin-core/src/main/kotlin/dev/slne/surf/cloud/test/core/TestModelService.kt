package dev.slne.surf.cloud.test.core

import dev.slne.surf.cloud.api.common.util.requiredService
import dev.slne.surf.cloud.test.api.TestModel
import it.unimi.dsi.fastutil.objects.ObjectSet

interface TestModelService {

    suspend fun findAll(): ObjectSet<TestModel>
    suspend fun findByName(name: String): TestModel?

    companion object {
        val INSTANCE = requiredService<TestModelService>()
    }

}

val testModelService get() = TestModelService.INSTANCE