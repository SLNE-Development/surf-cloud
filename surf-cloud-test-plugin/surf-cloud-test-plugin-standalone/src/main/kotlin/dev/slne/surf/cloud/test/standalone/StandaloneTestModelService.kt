package dev.slne.surf.cloud.test.standalone

import com.google.auto.service.AutoService
import dev.slne.surf.cloud.api.common.util.toObjectSet
import dev.slne.surf.cloud.test.core.TestModelService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
@AutoService(TestModelService::class)
class StandaloneTestModelService(private val repository: TestModelRepository) : TestModelService {

    override suspend fun findAll() = withContext(Dispatchers.IO) {
        println("Using impl to find all")
        repository.findAll().toObjectSet()
    }

    override suspend fun findByName(name: String) = withContext(Dispatchers.IO) {
        println("Using impl to find by name")
        repository.findByName(name)
    }

}