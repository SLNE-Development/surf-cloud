package dev.slne.surf.cloud.standalone.spring.entities.player

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.TimeZoneStorage
import org.hibernate.annotations.TimeZoneStorageType
import org.hibernate.type.SqlTypes
import java.io.Serial
import java.io.Serializable
import java.net.Inet4Address
import java.time.ZonedDateTime
import java.util.*

//@Entity
//@Table(name = "cloud_player")
//class CloudPlayerEntity : Serializable {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @JdbcTypeCode(SqlTypes.BIGINT)
//    @Column(nullable = false)
//    var id: Long? = null
//
//    @JdbcTypeCode(SqlTypes.CHAR)
//    @Column(name = "uuid", nullable = false, unique = true, length = 36)
//    var uuid: UUID? = null
//
//    @JdbcTypeCode(SqlTypes.CHAR)
//    @Column(name = "last_server")
//    var lastServer: String? = null
//
//    @TimeZoneStorage(TimeZoneStorageType.AUTO)
//    @Column(name = "last_seen")
//    var lastSeen: ZonedDateTime? = null
//
//    @Column(name = "last_ip_address")
//    var lastIpAddress: Inet4Address? = null
//
//
//    companion object {
//        @Serial
//        private const val serialVersionUID: Long = -7433875414223638042L
//    }
//}