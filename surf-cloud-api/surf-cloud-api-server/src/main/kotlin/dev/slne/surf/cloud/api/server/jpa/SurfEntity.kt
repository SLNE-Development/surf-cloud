package dev.slne.surf.cloud.api.server.jpa

import jakarta.persistence.*
import org.springframework.data.domain.Persistable
import org.springframework.data.jpa.domain.AbstractPersistable
import org.springframework.data.util.ProxyUtils
import org.springframework.lang.Nullable

/**
 * Base class for JPA entities in the Surf Cloud application.
 * Implements the [Persistable] interface
 * to provide consistent behavior for entity lifecycle operations.
 */
@MappedSuperclass
abstract class SurfEntity : Persistable<Long?> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Nullable
    @JvmField
    protected var id: Long? = null

    /**
     * Retrieves the entity's ID.
     *
     * @return The entity ID, or `null` if it has not been assigned yet.
     */
    override fun getId() = id

    /**
     * Sets the entity's ID.
     *
     * @param id The new ID value.
     */
    fun setId(id: Long?) {
        this.id = id
    }

    /**
     * Checks if the entity is new (not persisted).
     *
     * @return `true` if the entity is new; `false` otherwise.
     */
    @Transient
    override fun isNew(): Boolean = null == id

    /**
     * Returns a string representation of the entity, including its type and ID.
     *
     * @return A string describing the entity.
     */
    override fun toString(): String {
        return String.format(
            "Entity of type %s with id: %s",
            javaClass.name, getId()
        )
    }

    /**
     * Compares this entity with another for equality.
     * Two entities are considered equal if they are of the same type
     * and their IDs match.
     *
     * @param other The object to compare with.
     * @return `true` if the entities are equal; `false` otherwise.
     */
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

    /**
     * Computes the hash code for the entity, based on its ID.
     *
     * @return The hash code.
     */
    override fun hashCode(): Int {
        var hashCode = 17

        hashCode += if (null == getId()) 0 else getId().hashCode() * 31

        return hashCode
    }
}
