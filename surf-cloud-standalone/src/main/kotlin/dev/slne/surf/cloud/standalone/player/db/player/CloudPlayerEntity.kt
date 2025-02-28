package dev.slne.surf.cloud.standalone.player.db.player

import dev.slne.surf.cloud.api.server.jpa.SurfAuditableEntity
import dev.slne.surf.cloud.standalone.player.db.player.name.CloudPlayerNameHistoryEntity
import jakarta.persistence.Cacheable
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.TimeZoneStorage
import org.hibernate.annotations.TimeZoneStorageType
import org.hibernate.type.SqlTypes
import java.net.Inet4Address
import java.time.ZonedDateTime
import java.util.*


@Entity(name = "cloud_player")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
class CloudPlayerEntity(
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "uuid", nullable = false, unique = true, length = 36)
    val uuid: UUID,

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "last_server")
    var lastServer: String? = null,

    @TimeZoneStorage(TimeZoneStorageType.NORMALIZE_UTC)
    @Column(name = "last_seen")
    var lastSeen: ZonedDateTime? = null,

    @Column(name = "last_ip_address")
    var lastIpAddress: Inet4Address? = null,

    @OneToMany(mappedBy = "player", cascade = [CascadeType.ALL], orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    var nameHistories: MutableSet<CloudPlayerNameHistoryEntity> = mutableSetOf()
) : SurfAuditableEntity()