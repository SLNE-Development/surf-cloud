package dev.slne.surf.cloud.api.jpa

import jakarta.persistence.*
import org.springframework.data.domain.Persistable
import org.springframework.data.jpa.domain.AbstractPersistable
import org.springframework.data.util.ProxyUtils
import org.springframework.lang.Nullable

@MappedSuperclass
abstract class SurfEntity : Persistable<Long?> {
    @Suppress("PropertyName")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Nullable
    protected var _id: Long? = null

    override fun getId() = _id

    fun setId(id: Long?) {
        _id = id
    }

    @Transient
    override fun isNew(): Boolean = null == _id

    override fun toString(): String {
        return String.format(
            "Entity of type %s with id: %s",
            javaClass.name, getId()
        )
    }

    override fun equals(other: Any?): Boolean {
        if (null == other) {
            return false
        }

        if (this === other) {
            return true
        }

        if (javaClass != ProxyUtils.getUserClass(other)) {
            return false
        }

        val that = other as AbstractPersistable<*>

        return null != this.getId() && this.getId() == that.id
    }

    override fun hashCode(): Int {
        var hashCode = 17

        hashCode += if (null == getId()) 0 else getId().hashCode() * 31

        return hashCode
    }
}
