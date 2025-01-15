package dev.slne.surf.cloud.standalone.test

import dev.slne.surf.cloud.api.server.jpa.SurfEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.util.*

@Entity(name = "test_entity")
@Table(name = "test_entities")
class TestEntity(

) : SurfEntity() {

    @Column(
        name = "active",
        nullable = false
    )
    private var active: Boolean? = null

    @Column(
        name = "age",
        nullable = false
    )
    private var age: Int? = null

    @Column(
        name = "balance",
        nullable = false
    )
    private var balance: Double? = null

    @Column(
        name = "uuid",
        nullable = false,
        unique = true
    )
    private var uuid: UUID? = null

    @Column(
        name = "name",
        nullable = false,
        length = 16
    )
    private var name: String? = null

    @Column(
        name = "dumb",
        nullable = false
    )
    private var dumb: Boolean? = null

    @Column(name = "dummy")
    var dummy: String? = null
}
