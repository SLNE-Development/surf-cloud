package dev.slne.surf.cloud.standalone.player.db.player.name

import dev.slne.surf.cloud.api.server.jpa.SurfAuditableEntity
import dev.slne.surf.cloud.standalone.player.db.player.CloudPlayerEntity
import jakarta.persistence.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity(name = "cloud_player_name_history")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
class CloudPlayerNameHistoryEntity(
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "name", nullable = false, length = 16)
    val name: String,

    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "cloud_player_id")
    var player: CloudPlayerEntity? = null
) : SurfAuditableEntity()