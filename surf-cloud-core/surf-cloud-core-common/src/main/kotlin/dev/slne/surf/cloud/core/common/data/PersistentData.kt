package dev.slne.surf.cloud.core.common.data

import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.BinaryTagType

interface PersistentData<T> {
    fun value(): T?

    fun setValue(value: T?)

    operator fun contains(key: String): Boolean
    fun nonNull(): NonNullPersistentData<T> = NonNullPersistentData.of(this)

    operator fun getValue(thisRef: Any?, property: Any?): T? = value()
    operator fun setValue(thisRef: Any?, property: Any?, value: T?) = setValue(value)

    companion object {
        @JvmStatic
        fun <T : BinaryTag, D> data(
            key: String,
            type: BinaryTagType<T>,
            toValue: (T) -> D,
            toTag: (D) -> T
        ): PersistentData<D> = data(key, type, toValue, toTag, null)

        @JvmStatic
        fun <T : BinaryTag, D> data(
            key: String,
            type: BinaryTagType<T>,
            toValue: (T) -> D,
            toTag: (D) -> T,
            defaultValue: D?
        ): PersistentData<D> = PersistentDataImpl.data(key, type, toValue, toTag, defaultValue)

    }
}

interface NonNullPersistentData<T> {
    fun value(): T

    fun setValue(value: T)

    operator fun contains(key: String): Boolean

    operator fun getValue(thisRef: Any?, property: Any?): T = value()
    operator fun setValue(thisRef: Any?, property: Any?, value: T) = setValue(value)

    companion object {
        fun <T> of(data: PersistentData<T>) = object : NonNullPersistentData<T> {
            override fun value(): T = data.value()!!

            override fun setValue(value: T) {
                data.setValue(value)
            }

            override fun contains(key: String): Boolean = key in data
        }
    }
}


fun <T : BinaryTag, D> persistentData(
    key: String,
    type: BinaryTagType<T>,
    toValue: T.() -> D,
    toTag: (D) -> T,
    defaultValue: D? = null
): PersistentData<D> = PersistentDataImpl.data(key, type, toValue, toTag, defaultValue)
