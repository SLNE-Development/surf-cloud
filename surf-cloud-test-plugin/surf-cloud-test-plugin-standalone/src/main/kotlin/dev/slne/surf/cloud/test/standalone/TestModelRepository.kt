package dev.slne.surf.cloud.test.standalone

import dev.slne.surf.cloud.api.common.meta.SurfJpaRepository
import dev.slne.surf.cloud.test.api.TestModel
import org.springframework.data.jpa.repository.JpaRepository

@SurfJpaRepository
interface TestModelRepository : JpaRepository<TestModel, Long> {

    suspend fun findByName(name: String): TestModel?

}