package dev.slne.surf.cloud.test.api

import dev.slne.surf.cloud.api.common.jpa.SurfEntity
import dev.slne.surf.cloud.api.common.netty.network.codec.streamCodec
import dev.slne.surf.cloud.api.common.netty.protocol.buffer.SurfByteBuf
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "test_entities")
data class TestModel(
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "name", nullable = false, unique = true)
    var name: String
) : SurfEntity() {

    companion object {
        val STREAM_CODEC = streamCodec<SurfByteBuf, TestModel>({ buf, model ->
            buf.writeUtf(model.name)
        }, { buf ->
            TestModel(buf.readUtf())
        })
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TestModel) return false
        if (!super.equals(other)) return false

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "TestModel(name=$name)"
    }

}