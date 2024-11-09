package dev.slne.surf.cloud.core.data

import net.querz.nbt.tag.Tag
import kotlin.reflect.KClass

interface PersistentData<T> {
    fun value(): T?

    fun setValue(value: T?)

    operator fun contains(key: String): Boolean
    fun nonNull(): NonNullPersistentData<T> = NonNullPersistentData.of(this)

    companion object {
        @JvmStatic
        fun <T : Tag<D>, D> data(
            key: String,
            type: Class<T>,
            toValue: (T) -> D,
            toTag: (D) -> T
        ): PersistentData<D> = data(key, type, toValue, toTag, null)

        @JvmStatic
        fun <T : Tag<D>, D> data(
            key: String,
            type: Class<T>,
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


fun <T : Tag<D>, D> persistentData(
    key: String,
    type: KClass<T>,
    toValue: T.() -> D,
    toTag: (D) -> T,
    defaultValue: D? = null
): PersistentData<D> = PersistentDataImpl.data(key, type.java, toValue, toTag, defaultValue)


inline fun <reified T : Tag<D>, D> persistentData(
    key: String,
    noinline toTag: (D) -> T,
    noinline toValue: T.() -> D,
    defaultValue: D? = null
) = persistentData(key, T::class, toValue, toTag, defaultValue)
