package dev.slne.surf.cloud.standalone.player.db.repository

import dev.slne.surf.cloud.api.server.meta.SurfJpaRepository
import dev.slne.surf.cloud.standalone.player.db.player.CloudPlayerEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

@SurfJpaRepository
interface CloudPlayerRepository : JpaRepository<CloudPlayerEntity, Long> {

    @Query("select c from cloud_player c where c.uuid = ?1")
    fun findByUuid(uuid: UUID): CloudPlayerEntity?

}