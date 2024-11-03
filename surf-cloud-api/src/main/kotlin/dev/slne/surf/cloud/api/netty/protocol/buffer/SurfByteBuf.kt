@file:OptIn(ExperimentalContracts::class)

package dev.slne.surf.cloud.api.netty.protocol.buffer

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf.Companion
import dev.slne.surf.cloud.api.netty.protocol.buffer.decoder.DecodeFactory
import dev.slne.surf.cloud.api.netty.protocol.buffer.decoder.DecodeFactory.DecodeLongFactory
import dev.slne.surf.cloud.api.netty.protocol.buffer.ecoder.EncodeFactory
import dev.slne.surf.cloud.api.netty.protocol.buffer.ecoder.EncodeFactory.EncodeLongFactory
import dev.slne.surf.cloud.api.netty.protocol.buffer.types.Utf8String
import dev.slne.surf.cloud.api.netty.protocol.buffer.types.VarInt
import dev.slne.surf.cloud.api.util.fromJson
import io.netty.buffer.ByteBuf
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.EncoderException
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import okhttp3.internal.and
import org.intellij.lang.annotations.Subst
import java.io.*
import java.util.*
import java.util.function.Consumer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KClass

private const val NUMBER_BYTE: Byte = 0
private const val NUMBER_SHORT: Byte = 1
private const val NUMBER_INT: Byte = 2
private const val NUMBER_LONG: Byte = 3
private const val NUMBER_FLOAT: Byte = 4
private const val NUMBER_DOUBLE: Byte = 5

class SurfByteBuf(source: ByteBuf) : WrappedByteBuf(source) {
    companion object {
        private val GSON = Gson()

        /**
         * Read key key.
         *
         * @param buf the buf
         * @return the key
         */
        fun readKey(buf: ByteBuf): Key {
            @Subst("key:value") val keyString = readUtf(buf)
            return Key.key(keyString)
        }

        /**
         * Write key.
         *
         * @param buf the buf
         * @param key the key
         */
        fun writeKey(buf: ByteBuf, key: Key) {
            writeUtf(buf, key.asString())
        }

        fun writeSound(buf: ByteBuf, sound: Sound) {
            writeKey(buf, sound.name())
            writeEnum(buf, sound.source())
            buf.writeFloat(sound.volume())
            buf.writeFloat(sound.pitch())
            writeOptionalLong(buf, sound.seed())
        }

        fun readSound(buf: ByteBuf) = Sound.sound()
            .type(readKey(buf))
            .source(readEnum(buf, Sound.Source::class.java))
            .volume(buf.readFloat())
            .pitch(buf.readFloat())
            .seed(readOptionalLong(buf))
            .build()

        fun readComponent(buf: ByteBuf) = GsonComponentSerializer.gson().deserialize(readUtf(buf))
        fun writeComponent(buf: ByteBuf, component: Component) =
            writeUtf(buf, GsonComponentSerializer.gson().serialize(component))

        fun writeNumber(buf: ByteBuf, number: Number): Any = when (number) {
            is Byte -> {
                buf.writeByte(NUMBER_BYTE.toInt())
                buf.writeByte(number.toInt())
            }

            is Short -> {
                buf.writeByte(NUMBER_SHORT.toInt())
                buf.writeShort(number.toInt())
            }

            is Int -> {
                buf.writeByte(NUMBER_INT.toInt())
                writeVarInt(buf, number)
            }

            is Long -> {
                buf.writeByte(NUMBER_LONG.toInt())
                buf.writeLong(number)
            }

            is Float -> {
                buf.writeByte(NUMBER_FLOAT.toInt())
                buf.writeFloat(number)
            }

            is Double -> {
                buf.writeByte(NUMBER_DOUBLE.toInt())
                buf.writeDouble(number)
            }

            else -> throw EncoderException("Unsupported number type: ${number.javaClass}")
        }

        fun readNumber(buf: ByteBuf) = when (buf.readByte()) {
            NUMBER_BYTE -> buf.readByte()
            NUMBER_SHORT -> buf.readShort()
            NUMBER_INT -> readVarInt(buf)
            NUMBER_LONG -> buf.readLong()
            NUMBER_FLOAT -> buf.readFloat()
            NUMBER_DOUBLE -> buf.readDouble()
            else -> throw DecoderException("Unknown number type")
        }

        fun writeUnsigned(buf: ByteBuf, value: Byte) {
            checkEncoded(value >= 0) { "Value must be positive" }
            buf.writeByte(value and 0xFF)
        }

        fun writeUnsigned(buf: ByteBuf, value: Short) {
            checkEncoded(value >= 0) { "Value must be positive" }
            buf.writeShort(value and 0xFFFF)
        }

        fun writeUnsigned(buf: ByteBuf, value: Int) {
            checkEncoded(value >= 0) { "Value must be positive" }
            buf.writeInt(value and 0xFFFFFFFF.toInt())
        }

        fun writeUnsigned(buf: ByteBuf, value: Long) {
            checkEncoded(value >= 0) { "Value must be positive" }
            buf.writeLong(value)
        }

        fun writeUnsigned(buf: ByteBuf, value: Float) {
            checkEncoded(value >= 0) { "Value must be positive" }
            buf.writeFloat(value)
        }

        fun writeUnsigned(buf: ByteBuf, value: Double) {
            checkEncoded(value >= 0) { "Value must be positive" }
            buf.writeDouble(value)
        }

        fun readUnsignedByte(buf: ByteBuf) = buf.readUnsignedByte()
        fun readUnsignedShort(buf: ByteBuf) = buf.readUnsignedShort()
        fun readUnsignedInt(buf: ByteBuf) = buf.readUnsignedInt()
        fun readUnsignedLong(buf: ByteBuf) = buf.readLong()
        fun readUnsignedFloat(buf: ByteBuf) = buf.readFloat()
        fun readUnsignedDouble(buf: ByteBuf) = buf.readDouble()

        fun <T, C : MutableCollection<T>, B : ByteBuf> readCollection(
            buf: B,
            collectionFactory: (Int) -> C,
            decodeFactory: DecodeFactory<B, T>
        ): C {
            val size = buf.readVarInt()
            val collection = collectionFactory(size)

            for (i in 0 until size) {
                collection.add(decodeFactory.invoke(buf))
            }

            return collection
        }

        fun <T, B : ByteBuf> writeCollection(
            buf: B,
            collection: Collection<T>,
            encodeFactory: EncodeFactory<in B, T>
        ) {
            buf.writeVarInt(collection.size)

            for (element in collection) {
                encodeFactory.invoke(buf, element)
            }
        }

        inline fun <reified T, B : ByteBuf> readArray(
            buf: B,
            decodeFactory: DecodeFactory<in B, T>
        ) = Array(buf.readVarInt()) { decodeFactory.invoke(buf) }

        fun <T, B : ByteBuf> readArray(
            buf: B,
            arrayFactory: (Int) -> Array<T>,
            decodeFactory: DecodeFactory<in B, T>
        ): Array<T> {
            val size = buf.readVarInt()
            val array = arrayFactory(size)

            for (i in 0 until size) {
                array[i] = decodeFactory.invoke(buf)
            }

            return array
        }

        fun <B : ByteBuf> readIntArray(buf: B) = IntArray(buf.readVarInt()).apply {
            for (i in indices) {
                this[i] = buf.readVarInt()
            }
        }

        fun <B : ByteBuf> readLongArray(buf: B) = LongArray(buf.readVarInt()).apply {
            for (i in indices) {
                this[i] = buf.readLong()
            }
        }

        fun <B : ByteBuf> readFloatArray(buf: B) = FloatArray(buf.readVarInt()).apply {
            for (i in indices) {
                this[i] = buf.readFloat()
            }
        }

        fun <B : ByteBuf> readDoubleArray(buf: B) = DoubleArray(buf.readVarInt()).apply {
            for (i in indices) {
                this[i] = buf.readDouble()
            }
        }

        fun <B : ByteBuf> readBooleanArray(buf: B) = BooleanArray(buf.readVarInt()).apply {
            for (i in indices) {
                this[i] = buf.readBoolean()
            }
        }

        fun <B : ByteBuf> readByteArray(buf: B) = ByteArray(buf.readVarInt()).apply {
            for (i in indices) {
                this[i] = buf.readByte()
            }
        }

        fun <B : ByteBuf> readShortArray(buf: B) = ShortArray(buf.readVarInt()).apply {
            for (i in indices) {
                this[i] = buf.readShort()
            }
        }

        fun <B : ByteBuf> readCharArray(buf: B) = CharArray(buf.readVarInt()).apply {
            for (i in indices) {
                this[i] = buf.readChar()
            }
        }

        fun <B : ByteBuf> readStringArray(buf: B) = Array(buf.readVarInt()) { readUtf(buf) }
        fun <B : ByteBuf> readUuidArray(buf: B) = Array(buf.readVarInt()) { readUuid(buf) }

        fun <T, B : ByteBuf> writeArray(
            buf: B,
            array: Array<T>,
            encodeFactory: EncodeFactory<in B, T>
        ) {
            buf.writeVarInt(array.size)

            for (element in array) {
                encodeFactory.invoke(buf, element)
            }
        }

        fun <B : ByteBuf> writeIntArray(buf: B, array: IntArray) {
            buf.writeVarInt(array.size)
            for (element in array) {
                buf.writeVarInt(element)
            }
        }

        fun <B : ByteBuf> writeLongArray(buf: B, array: LongArray) {
            buf.writeVarInt(array.size)
            for (element in array) {
                buf.writeLong(element)
            }
        }

        fun <B : ByteBuf> writeFloatArray(buf: B, array: FloatArray) {
            buf.writeVarInt(array.size)
            for (element in array) {
                buf.writeFloat(element)
            }
        }

        fun <B : ByteBuf> writeDoubleArray(buf: B, array: DoubleArray) {
            buf.writeVarInt(array.size)
            for (element in array) {
                buf.writeDouble(element)
            }
        }

        fun <B : ByteBuf> writeBooleanArray(buf: B, array: BooleanArray) {
            buf.writeVarInt(array.size)
            for (element in array) {
                buf.writeBoolean(element)
            }
        }

        fun <B : ByteBuf> writeByteArray(buf: B, array: ByteArray) {
            buf.writeVarInt(array.size)
            for (element in array) {
                buf.writeByte(element.toInt())
            }
        }

        fun <B : ByteBuf> writeShortArray(buf: B, array: ShortArray) {
            buf.writeVarInt(array.size)
            for (element in array) {
                buf.writeShort(element.toInt())
            }
        }

        fun <B : ByteBuf> writeCharArray(buf: B, array: CharArray) {
            buf.writeVarInt(array.size)
            for (element in array) {
                buf.writeChar(element.code)
            }
        }

        fun <B : ByteBuf> writeStringArray(buf: B, array: Array<String>) {
            buf.writeVarInt(array.size)
            for (element in array) {
                writeUtf(buf, element)
            }
        }

        fun <B : ByteBuf> writeUuidArray(buf: B, array: Array<UUID>) {
            buf.writeVarInt(array.size)
            for (element in array) {
                writeUuid(buf, element)
            }
        }

        fun <T, B : ByteBuf> readList(
            buf: B,
            decodeFactory: DecodeFactory<B, T>
        ) = readCollection(buf, { ObjectArrayList(it) }, decodeFactory)

        fun <K, V, M : MutableMap<K, V>, B : ByteBuf> readMap(
            buf: B,
            mapFactory: (Int) -> M,
            keyDecodeFactory: DecodeFactory<in B, K>,
            valueDecodeFactory: DecodeFactory<in B, V>
        ): M {
            val size = buf.readVarInt()
            val map = mapFactory(size)

            for (i in 0 until size) {
                val key = keyDecodeFactory.invoke(buf)
                val value = valueDecodeFactory.invoke(buf)

                map[key] = value
            }

            return map
        }

        fun <K, V, B : ByteBuf> readMap(
            buf: B,
            keyDecodeFactory: DecodeFactory<in B, K>,
            valueDecodeFactory: DecodeFactory<in B, V>
        ) = readMap(buf, { Object2ObjectOpenHashMap(it) }, keyDecodeFactory, valueDecodeFactory)

        fun <K, V, B : ByteBuf> writeMap(
            buf: B,
            map: Map<K, V>,
            keyEncodeFactory: EncodeFactory<in B, K>,
            valueEncodeFactory: EncodeFactory<in B, V>
        ) {
            buf.writeVarInt(map.size)

            for ((key, value) in map) {
                keyEncodeFactory.invoke(buf, key)
                valueEncodeFactory.invoke(buf, value)
            }
        }

        inline fun <reified E : Enum<E>> readEnumSet(buf: ByteBuf) = readEnumSet(buf, E::class.java)
        fun <E : Enum<E>> readEnumSet(
            buf: ByteBuf,
            enumClass: KClass<E>
        ) = readEnumSet(buf, enumClass.java)

        fun <E : Enum<E>> readEnumSet(
            buf: ByteBuf,
            enumClass: Class<E>
        ): EnumSet<E> {
            val values = enumClass.enumConstants
            val bitSet = readFixedBitSet(buf, values.size)
            val enumSet = EnumSet.noneOf(enumClass)

            for (i in values.indices) {
                if (bitSet[i]) {
                    enumSet.add(values[i])
                }
            }

            return enumSet
        }

        fun <E : Enum<E>> writeEnumSet(
            buf: ByteBuf,
            enumSet: EnumSet<E>,
            enumClass: Class<E>
        ) {
            val values = enumClass.enumConstants
            val bitSet = BitSet(values.size)

            for (i in values.indices) {
                bitSet[i] = enumSet.contains(values[i])
            }

            writeFixedBitSet(buf, bitSet, values.size)
        }

        fun <E : Enum<E>> writeEnumSet(
            buf: ByteBuf,
            enumSet: EnumSet<E>,
            enumClass: KClass<E>
        ) = writeEnumSet(buf, enumSet, enumClass.java)

        fun <E : Enum<E>> readEnum(buf: ByteBuf, enumClass: Class<E>): E =
            enumClass.enumConstants[buf.readVarInt()]

        fun <E : Enum<E>> writeEnum(buf: ByteBuf, value: E) = buf.writeVarInt(value.ordinal)

        fun writeUuid(buf: ByteBuf, uuid: UUID) {
            buf.writeLong(uuid.mostSignificantBits)
            buf.writeLong(uuid.leastSignificantBits)
        }

        fun readUuid(buf: ByteBuf) = UUID(buf.readLong(), buf.readLong())
        fun readBitSet(buf: ByteBuf) = BitSet.valueOf(readLongArray(buf))
        fun writeBitSet(buf: ByteBuf, bitSet: BitSet) =
            writeLongArray(buf, bitSet.toLongArray())

        fun readFixedBitSet(buf: ByteBuf, size: Int) =
            ByteArray(positiveCeilDiv(size, Byte.SIZE_BITS)).run {
                buf.readBytes(this)
                BitSet.valueOf(this)
            }

        fun writeFixedBitSet(buf: ByteBuf, bitSet: BitSet, size: Int) =
            bitSet.length().let { bitSetSize ->
                checkEncoded(bitSetSize <= size) { "BitSet is larger than expected size ($bitSetSize>$size)" }
                buf.writeBytes(bitSet.toByteArray().copyOf(positiveCeilDiv(size, Byte.SIZE_BITS)))
            }

        @JvmOverloads
        fun readUtf(buf: ByteBuf, maxLength: Int = Int.MAX_VALUE) = Utf8String.read(buf, maxLength)

        @JvmOverloads
        fun writeUtf(buf: ByteBuf, string: String, maxLength: Int = Int.MAX_VALUE) =
            Utf8String.write(buf, string, maxLength)

        fun readVarInt(buf: ByteBuf) = VarInt.readVarInt(buf)
        fun writeVarInt(buf: ByteBuf, value: Int) = VarInt.writeVarInt(buf, value)

        fun <B : ByteBuf> writeWithCount(buf: B, count: Int, writer: (B) -> Unit) = buf.run {
            writeVarInt(count)
            for (i in 0 until count) {
                writer(this)
            }
        }

        fun <B : ByteBuf> writeWithCount(buf: B, count: Int, writer: Consumer<B>) =
            writeWithCount(buf, count) { writer.accept(it) }

        fun <B : ByteBuf> readWithCount(buf: B, reader: (B) -> Unit) = buf.run {
            for (i in 0 until readVarInt()) {
                reader(this)
            }
        }

        fun <B : ByteBuf> readWithCount(buf: B, reader: Consumer<B>) =
            readWithCount(buf) { reader.accept(it) }

        fun <T, B : ByteBuf> readNullable(
            buf: B,
            decodeFactory: DecodeFactory<in B, T>
        ): T? = if (buf.readBoolean()) decodeFactory(buf) else null

        fun <T, B : ByteBuf> writeNullable(
            buf: B,
            value: T?,
            encodeFactory: EncodeFactory<in B, T>
        ) {
            buf.writeBoolean(value != null)

            if (value != null) {
                encodeFactory(buf, value)
            }
        }

        fun <T, B : ByteBuf> writeOptional(
            buf: B,
            optional: Optional<T>,
            encodeFactory: EncodeFactory<in B, T>
        ) {
            buf.writeBoolean(optional.isPresent)
            optional.ifPresent { encodeFactory(buf, it) }
        }

        fun <T : Any, B : ByteBuf> readOptional(
            buf: B,
            decodeFactory: DecodeFactory<in B, T>
        ) = if (buf.readBoolean()) Optional.of(decodeFactory(buf)) else Optional.empty()

        @JvmOverloads
        fun <B : ByteBuf> writeOptionalLong(
            buf: B,
            optional: OptionalLong,
            encodeFactory: EncodeLongFactory<in B> =
                EncodeLongFactory { buf, value -> buf.writeLong(value) }
        ) {
            buf.writeBoolean(optional.isPresent)
            optional.ifPresent { encodeFactory.encodeLong(buf, it) }
        }

        @JvmOverloads
        fun <B : ByteBuf> readOptionalLong(
            buf: B,
            decodeFactory: DecodeLongFactory<in B> =
                DecodeLongFactory { buf -> buf.readLong() }
        ) =
            if (buf.readBoolean()) OptionalLong.of(decodeFactory.decodeLong(buf)) else OptionalLong.empty()


        @Deprecated("Use codec instead")
        fun <B : ByteBuf> writeSerializable(buf: B, serializable: Serializable) =
            ByteArrayOutputStream().use { bout ->
                ObjectOutputStream(bout).use { out ->
                    try {
                        out.writeObject(serializable)
                        out.flush()

                        val bytes = bout.toByteArray()
                        buf.writeVarInt(bytes.size)
                        buf.writeBytes(bytes)
                    } catch (e: IOException) {
                        throw EncoderException("Failed to write serializable", e)
                    }

                }
            }

        @Deprecated("Use codec instead")
        fun <T : Serializable, B : ByteBuf> readSerializable(
            buf: B,
            clazz: Class<out T>
        ): T {
            val bytes = ByteArray(buf.readVarInt())
            buf.readBytes(bytes)
            return try {
                ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() as T }
            } catch (e: IOException) {
                throw DecoderException("Failed to read serializable", e)
            } catch (e: ClassNotFoundException) {
                throw DecoderException("Failed to read serializable", e)
            } catch (e: ClassCastException) {
                throw DecoderException("Failed to cast serializable! Expected: $clazz", e)
            }
        }

        @Suppress("DEPRECATION")
        @Deprecated("Use codec instead")
        inline fun <reified T : Serializable, B : ByteBuf> readSerializable(
            buf: B,
        ): T = readSerializable(buf, T::class.java)
    }


    // region [Instance methods]
    // @formatter:off

    @Throws(DecoderException::class)
    fun <T> readJsonWithCodec(codec: Codec<T>) :T {
        val jsonElement = fromJson<JsonElement>(GSON, readUtf())
        val result = codec.parse(JsonOps.INSTANCE, jsonElement)

        return result.getOrThrow { DecoderException("Failed to decode json: $it") }
    }


    @JvmOverloads
    @Throws(EncoderException::class)
    fun <T>writeJsonWithCodec(codec: Codec<T>, value: T, maxLength: Int = Int.MAX_VALUE) {
        val result = codec.encodeStart(JsonOps.INSTANCE, value)

        this.writeUtf(GSON.toJson(result.getOrThrow{EncoderException("Failed to encode: $it ${value.toString()}")}), maxLength)
    }


    @Suppress("DEPRECATION")
    @Deprecated("Use codec instead")
    fun writeSerializable(serializable: Serializable) = writeSerializable(this, serializable)
    @Suppress("DEPRECATION")
    @Deprecated("Use codec instead")
    fun <T : Serializable> readSerializable(clazz: Class<out T>) = readSerializable(this, clazz)
    @Suppress("DEPRECATION")
    @Deprecated("Use codec instead")
    inline fun <reified T : Serializable> readSerializable() = readSerializable(this, T::class.java)
    fun readKey() = readKey(this)
    fun writeKey(key: Key) = writeKey(this, key)
    fun writeSound(sound: Sound) = writeSound(this, sound)
    fun readSound() = readSound(this)
    fun readComponent() = readComponent(this)
    fun writeComponent(component: Component) = writeComponent(this, component)
    fun writeNumber(number: Number) = writeNumber(this, number)
    fun readNumber() = readNumber(this)
    fun writeUnsigned(value: Byte) = writeUnsigned(this, value)
    fun writeUnsigned(value: Short) = writeUnsigned(this, value)
    fun writeUnsigned(value: Int) = writeUnsigned(this, value)
    fun writeUnsigned(value: Long) = writeUnsigned(this, value)
    fun writeUnsigned(value: Float) = writeUnsigned(this, value)
    fun writeUnsigned(value: Double) = writeUnsigned(this, value)
    fun readUnsignedLong() = readUnsignedLong(this)
    fun readUnsignedFloat() = readUnsignedFloat(this)
    fun readUnsignedDouble() = readUnsignedDouble(this)
    fun <T, C : MutableCollection<T>> readCollection(collectionFactory: (Int) -> C, decodeFactory: DecodeFactory<SurfByteBuf, T>) = readCollection(this, collectionFactory, decodeFactory)
    fun <T, C : MutableCollection<T>> readCollectionJsonWithCodec(codec: Codec<T>, collectionFactory: (Int) -> C) = readCollection(collectionFactory) { it.readJsonWithCodec(codec) }
    fun <T> writeCollection(collection: Collection<T>, encodeFactory: EncodeFactory<in SurfByteBuf, T>) = writeCollection(this, collection, encodeFactory)
    fun <T> writeCollectionJsonWithCodec(codec: Codec<T>, collection: Collection<T>) = writeCollection(collection) { buf, element -> buf.writeJsonWithCodec(codec, element) }
    inline fun <reified T> readArray(decodeFactory: DecodeFactory<in SurfByteBuf, T>) = readArray(this, decodeFactory)
    fun <T> readArray(arrayFactory: (Int) -> Array<T>, decodeFactory: DecodeFactory<in SurfByteBuf, T>) = readArray(this, arrayFactory, decodeFactory)
    fun readIntArray() = readIntArray(this)
    fun readLongArray() = readLongArray(this)
    fun readFloatArray() = readFloatArray(this)
    fun readDoubleArray() = readDoubleArray(this)
    fun readBooleanArray() = readBooleanArray(this)
    fun readByteArray() = readByteArray(this)
    fun readShortArray() = readShortArray(this)
    fun readCharArray() = readCharArray(this)
    fun readStringArray() = readStringArray(this)
    fun readUuidArray() = readUuidArray(this)

    @Throws(DecoderException::class)
    fun <T> readCodecJsonArray(
        codec: Codec<T>,
        arrayFactory: (Int) -> Array<T>
    ): Array<T> {
        val array = arrayFactory(readVarInt())

        for (i in array.indices) {
            array[i] = readJsonWithCodec(codec)
        }

        return array
    }

    inline fun <reified T> readCodecJsonArray(codec: Codec<T>) = Array(readVarInt()) { readJsonWithCodec(codec) }
    fun <T> writeArray(array: Array<T>, encodeFactory: EncodeFactory<in SurfByteBuf, T>) = writeArray(this, array, encodeFactory)
    fun writeIntArray(array: IntArray) = writeIntArray(this, array)
    fun writeLongArray(array: LongArray) = writeLongArray(this, array)
    fun writeFloatArray(array: FloatArray) = writeFloatArray(this, array)
    fun writeDoubleArray(array: DoubleArray) = writeDoubleArray(this, array)
    fun writeBooleanArray(array: BooleanArray) = writeBooleanArray(this, array)
    fun writeByteArray(array: ByteArray) = writeByteArray(this, array)
    fun writeShortArray(array: ShortArray) = writeShortArray(this, array)
    fun writeCharArray(array: CharArray) = writeCharArray(this, array)
    fun writeStringArray(array: Array<String>) = writeStringArray(this, array)
    fun writeUuidArray(array: Array<UUID>) = writeUuidArray(this, array)

    @Throws(EncoderException::class)
    fun <T> writeCodecJsonArray(
        codec: Codec<T>,
        array: Array<T>
    ) {
        writeVarInt(array.size)

        for (element in array) {
            writeJsonWithCodec(codec, element)
        }
    }

    fun <T> readList(decodeFactory: DecodeFactory<SurfByteBuf, T>) = readList(this, decodeFactory)
    fun <T> readListJsonWithCodec(codec: Codec<T>) = readList { it.readJsonWithCodec(codec) }
    fun <K, V> readMap(keyDecodeFactory: DecodeFactory<in SurfByteBuf, K>, valueDecodeFactory: DecodeFactory<in SurfByteBuf, V>) = readMap(this, keyDecodeFactory, valueDecodeFactory)
    fun <K, V, M : MutableMap<K, V>> readMap(mapFactory: (Int) -> M, keyDecodeFactory: DecodeFactory<in SurfByteBuf, K>, valueDecodeFactory: DecodeFactory<in SurfByteBuf, V>) = readMap(this, mapFactory, keyDecodeFactory, valueDecodeFactory)
    fun <K, V> writeMap(map: Map<K, V>, keyEncodeFactory: EncodeFactory<in SurfByteBuf, K>, valueEncodeFactory: EncodeFactory<in SurfByteBuf, V>) = writeMap(this, map, keyEncodeFactory, valueEncodeFactory)
    fun <E : Enum<E>> readEnumSet(enumClass: Class<E>) = readEnumSet(this, enumClass)
    fun <E : Enum<E>> readEnumSet(enumClass: KClass<E>) = readEnumSet(this, enumClass)
    fun <E : Enum<E>> writeEnumSet(enumSet: EnumSet<E>, enumClass: Class<E>) = writeEnumSet(this, enumSet, enumClass)
    fun <E : Enum<E>> writeEnumSet(enumSet: EnumSet<E>, enumClass: KClass<E>) = writeEnumSet(this, enumSet, enumClass)
    fun <E : Enum<E>> readEnum(enumClass: Class<E>) = readEnum(this, enumClass)
    fun <E: Enum<E>> readEnum(enumClass: KClass<E>) = readEnum(enumClass.java)
    fun <E : Enum<E>> writeEnum(value: E) = writeEnum(this, value)
    fun writeUuid(uuid: UUID) = writeUuid(this, uuid)
    fun readUuid() = readUuid(this)
    fun readBitSet() = readBitSet(this)
    fun writeBitSet(bitSet: BitSet) = writeBitSet(this, bitSet)
    fun readFixedBitSet(size: Int) = readFixedBitSet(this, size)
    fun writeFixedBitSet(bitSet: BitSet, size: Int) = writeFixedBitSet(this, bitSet, size)
    @JvmOverloads
    fun readUtf(maxLength: Int = Int.MAX_VALUE) = readUtf(this, maxLength)
    @JvmOverloads
    fun writeUtf(string: String, maxLength: Int = Int.MAX_VALUE) = writeUtf(this, string, maxLength)
    fun readVarInt() = readVarInt(this)
    fun writeVarInt(value: Int) = writeVarInt(this, value)
    fun <T> readNullable(decodeFactory: DecodeFactory<in SurfByteBuf, T>) = readNullable(this, decodeFactory)
    fun readNullableBoolean() = readNullable(this) { it.readBoolean() }
    fun readNullableByte() = readNullable(this) { it.readByte() }
    fun readNullableShort() = readNullable(this) { it.readShort() }
    fun readNullableInt() = readNullable(this) { it.readVarInt() }
    fun readNullableLong() = readNullable(this) { it.readLong() }
    fun readNullableFloat() = readNullable(this) { it.readFloat() }
    fun readNullableDouble() = readNullable(this) { it.readDouble() }
    fun readNullableChar() = readNullable(this) { it.readChar() }
    fun readNullableString() = readNullable(this) { readUtf(it) }
    fun readNullableUuid() = readNullable(this) { readUuid(it) }
    fun <T> writeNullable(value: T?, encodeFactory: EncodeFactory<in SurfByteBuf, T>) = writeNullable(this, value, encodeFactory)
    fun writeNullable(value: Boolean?) = writeNullable(this, value) { buf, bool -> buf.writeBoolean(bool) }
    fun writeNullable(value: Byte?) = writeNullable(this, value) { buf, byte -> buf.writeByte(byte.toInt()) }
    fun writeNullable(value: Short?) = writeNullable(this, value) { buf, short -> buf.writeShort(short.toInt()) }
    fun writeNullable(value: Int?) = writeNullable(this, value) { buf, int -> buf.writeVarInt(int) }
    fun writeNullable(value: Long?) = writeNullable(this, value) { buf, long -> buf.writeLong(long) }
    fun writeNullable(value: Float?) = writeNullable(this, value) { buf, float -> buf.writeFloat(float) }
    fun writeNullable(value: Double?) = writeNullable(this, value) { buf, double -> buf.writeDouble(double) }
    fun writeNullable(value: Char?) = writeNullable(this, value) { buf, char -> buf.writeChar(char.code) }
    fun writeNullable(value: String?) = writeNullable(this, value) { buf, string -> writeUtf(buf, string) }
    fun writeNullable(value: UUID?) = writeNullable(this, value) {buf, uuid ->  writeUuid(buf, uuid) }
    fun <T> writeOptional(optional: Optional<T>, encodeFactory: EncodeFactory<in SurfByteBuf, T>) = writeOptional(this, optional, encodeFactory)
    fun <T : Any> readOptional(decodeFactory: DecodeFactory<in SurfByteBuf, T>) = readOptional(this, decodeFactory)
    fun writeOptionalLong(optional: OptionalLong) = writeOptionalLong(this, optional)
    fun readOptionalLong() = readOptionalLong(this)
    fun writeWithCount(count: Int, writer: (SurfByteBuf) -> Unit) = writeWithCount(this, count, writer)
    fun writeWithCount(count: Int, writer: Consumer<SurfByteBuf>) = writeWithCount(this, count, writer)
    fun readWithCount(reader: (SurfByteBuf) -> Unit) = readWithCount(this, reader)
    fun readWithCount(reader: Consumer<SurfByteBuf>) = readWithCount(this, reader)
    // @formatter:on
// endregion

}

private fun positiveCeilDiv(a: Int, b: Int) = -Math.floorDiv(-a, b)

// Preconditions
inline fun checkEncoded(value: Boolean, message: () -> Any) {
    contract {
        returns() implies value
    }

    if (!value) throw EncoderException(message().toString())
}

inline fun checkDecoded(value: Boolean, message: () -> Any) {
    contract {
        returns() implies value
    }

    if (!value) throw DecoderException(message().toString())
}

// region ByteBuf extensions
fun ByteBuf.writeVarInt(value: Int) = SurfByteBuf.writeVarInt(this, value)
fun ByteBuf.readVarInt() = SurfByteBuf.readVarInt(this)

fun ByteBuf.readKey() = SurfByteBuf.readKey(this)
fun ByteBuf.writeKey(key: Key) = SurfByteBuf.writeKey(this, key)
fun ByteBuf.writeSound(sound: Sound) = SurfByteBuf.writeSound(this, sound)
fun ByteBuf.readSound() = SurfByteBuf.readSound(this)
fun ByteBuf.readComponent() = SurfByteBuf.readComponent(this)
fun ByteBuf.writeComponent(component: Component) = SurfByteBuf.writeComponent(this, component)
fun ByteBuf.writeNumber(number: Number) = SurfByteBuf.writeNumber(this, number)
fun ByteBuf.readNumber() = SurfByteBuf.readNumber(this)
fun ByteBuf.writeUnsigned(value: Byte) = SurfByteBuf.writeUnsigned(this, value)
fun ByteBuf.writeUnsigned(value: Short) = SurfByteBuf.writeUnsigned(this, value)
fun ByteBuf.writeUnsigned(value: Int) = SurfByteBuf.writeUnsigned(this, value)
fun ByteBuf.writeUnsigned(value: Long) = SurfByteBuf.writeUnsigned(this, value)
fun ByteBuf.writeUnsigned(value: Float) = SurfByteBuf.writeUnsigned(this, value)
fun ByteBuf.writeUnsigned(value: Double) = SurfByteBuf.writeUnsigned(this, value)
fun ByteBuf.readUnsignedLong() = SurfByteBuf.readUnsignedLong(this)
fun ByteBuf.readUnsignedFloat() = SurfByteBuf.readUnsignedFloat(this)
fun ByteBuf.readUnsignedDouble() = SurfByteBuf.readUnsignedDouble(this)
fun <B : ByteBuf, T, C : MutableCollection<T>> B.readCollection(
    collectionFactory: (Int) -> C,
    decodeFactory: DecodeFactory<B, T>
) =
    SurfByteBuf.readCollection(this, collectionFactory, decodeFactory)

fun <B : ByteBuf, T> B.writeCollection(
    collection: Collection<T>,
    encodeFactory: EncodeFactory<B, T>
) =
    SurfByteBuf.writeCollection(this, collection, encodeFactory)

inline fun <B : ByteBuf, reified T> B.readArray(decodeFactory: DecodeFactory<B, T>) =
    SurfByteBuf.readArray(this, decodeFactory)

fun <B : ByteBuf, T> B.readArray(
    arrayFactory: (Int) -> Array<T>,
    decodeFactory: DecodeFactory<B, T>
) =
    SurfByteBuf.readArray(this, arrayFactory, decodeFactory)

fun ByteBuf.readIntArray() = SurfByteBuf.readIntArray(this)
fun ByteBuf.readLongArray() = SurfByteBuf.readLongArray(this)
fun ByteBuf.readFloatArray() = SurfByteBuf.readFloatArray(this)
fun ByteBuf.readDoubleArray() = SurfByteBuf.readDoubleArray(this)
fun ByteBuf.readBooleanArray() = SurfByteBuf.readBooleanArray(this)
fun ByteBuf.readByteArray() = SurfByteBuf.readByteArray(this)
fun ByteBuf.readShortArray() = SurfByteBuf.readShortArray(this)
fun ByteBuf.readCharArray() = SurfByteBuf.readCharArray(this)
fun ByteBuf.readStringArray() = SurfByteBuf.readStringArray(this)
fun ByteBuf.readUuidArray() = SurfByteBuf.readUuidArray(this)
fun <B : ByteBuf, T> B.writeArray(array: Array<T>, encodeFactory: EncodeFactory<B, T>) =
    SurfByteBuf.writeArray(this, array, encodeFactory)

fun ByteBuf.writeIntArray(array: IntArray) = SurfByteBuf.writeIntArray(this, array)
fun ByteBuf.writeLongArray(array: LongArray) = SurfByteBuf.writeLongArray(this, array)
fun ByteBuf.writeFloatArray(array: FloatArray) = SurfByteBuf.writeFloatArray(this, array)
fun ByteBuf.writeDoubleArray(array: DoubleArray) = SurfByteBuf.writeDoubleArray(this, array)
fun ByteBuf.writeBooleanArray(array: BooleanArray) = SurfByteBuf.writeBooleanArray(this, array)
fun ByteBuf.writeByteArray(array: ByteArray) = SurfByteBuf.writeByteArray(this, array)
fun ByteBuf.writeShortArray(array: ShortArray) = SurfByteBuf.writeShortArray(this, array)
fun ByteBuf.writeCharArray(array: CharArray) = SurfByteBuf.writeCharArray(this, array)
fun ByteBuf.writeStringArray(array: Array<String>) = SurfByteBuf.writeStringArray(this, array)
fun ByteBuf.writeUuidArray(array: Array<UUID>) = SurfByteBuf.writeUuidArray(this, array)
fun <B : ByteBuf, T> B.readList(decodeFactory: DecodeFactory<B, T>) =
    SurfByteBuf.readList(this, decodeFactory)

fun <B : ByteBuf, K, V> B.readMap(
    keyDecodeFactory: DecodeFactory<B, K>,
    valueDecodeFactory: DecodeFactory<B, V>
) =
    SurfByteBuf.readMap(this, keyDecodeFactory, valueDecodeFactory)

fun <B : ByteBuf, K, V, M : MutableMap<K, V>> B.readMap(
    mapFactory: (Int) -> M,
    keyDecodeFactory: DecodeFactory<B, K>,
    valueDecodeFactory: DecodeFactory<B, V>
) =
    SurfByteBuf.readMap(this, mapFactory, keyDecodeFactory, valueDecodeFactory)

fun <B : ByteBuf, K, V> B.writeMap(
    map: Map<K, V>,
    keyEncodeFactory: EncodeFactory<B, K>,
    valueEncodeFactory: EncodeFactory<B, V>
) =
    SurfByteBuf.writeMap(this, map, keyEncodeFactory, valueEncodeFactory)

fun <E : Enum<E>> ByteBuf.readEnumSet(enumClass: Class<E>) =
    SurfByteBuf.readEnumSet(this, enumClass)

fun <E : Enum<E>> ByteBuf.readEnumSet(enumClass: KClass<E>) =
    SurfByteBuf.readEnumSet(this, enumClass)

fun <E : Enum<E>> ByteBuf.writeEnumSet(enumSet: EnumSet<E>, enumClass: Class<E>) =
    SurfByteBuf.writeEnumSet(this, enumSet, enumClass)

fun <E : Enum<E>> ByteBuf.writeEnumSet(enumSet: EnumSet<E>, enumClass: KClass<E>) =
    SurfByteBuf.writeEnumSet(this, enumSet, enumClass)

fun <E : Enum<E>> ByteBuf.readEnum(enumClass: Class<E>) = SurfByteBuf.readEnum(this, enumClass)
fun <E : Enum<E>> ByteBuf.writeEnum(value: E) = SurfByteBuf.writeEnum(this, value)
fun ByteBuf.writeUuid(uuid: UUID) = SurfByteBuf.writeUuid(this, uuid)
fun ByteBuf.readUuid() = SurfByteBuf.readUuid(this)
fun ByteBuf.readBitSet() = SurfByteBuf.readBitSet(this)
fun ByteBuf.writeBitSet(bitSet: BitSet) = SurfByteBuf.writeBitSet(this, bitSet)
fun ByteBuf.readFixedBitSet(size: Int) = SurfByteBuf.readFixedBitSet(this, size)
fun ByteBuf.writeFixedBitSet(bitSet: BitSet, size: Int) =
    SurfByteBuf.writeFixedBitSet(this, bitSet, size)

fun ByteBuf.readUtf(maxLength: Int = Int.MAX_VALUE) = SurfByteBuf.readUtf(this, maxLength)
fun ByteBuf.writeUtf(string: String, maxLength: Int = Int.MAX_VALUE) =
    SurfByteBuf.writeUtf(this, string, maxLength)

fun <B : ByteBuf, T> B.readNullable(decodeFactory: DecodeFactory<B, T>) =
    SurfByteBuf.readNullable(this, decodeFactory)

fun ByteBuf.readNullableBoolean() = SurfByteBuf.readNullable(this) { it.readBoolean() }
fun ByteBuf.readNullableByte() = SurfByteBuf.readNullable(this) { it.readByte() }
fun ByteBuf.readNullableShort() = SurfByteBuf.readNullable(this) { it.readShort() }

@Deprecated("Use varInt instead", ReplaceWith("readNullableVarInt()"))
fun ByteBuf.readNullableInt() = SurfByteBuf.readNullable(this) { it.readInt() }
fun ByteBuf.readNullableVarInt() = SurfByteBuf.readNullable(this) { it.readVarInt() }
fun ByteBuf.readNullableLong() = SurfByteBuf.readNullable(this) { it.readLong() }
fun ByteBuf.readNullableFloat() = SurfByteBuf.readNullable(this) { it.readFloat() }
fun ByteBuf.readNullableDouble() = SurfByteBuf.readNullable(this) { it.readDouble() }
fun ByteBuf.readNullableChar() = SurfByteBuf.readNullable(this) { it.readChar() }
fun ByteBuf.readNullableString() = SurfByteBuf.readNullable(this) { SurfByteBuf.readUtf(it) }
fun ByteBuf.readNullableUuid() = SurfByteBuf.readNullable(this) { SurfByteBuf.readUuid(it) }
fun <B : ByteBuf, T> B.writeNullable(value: T?, encodeFactory: EncodeFactory<B, T>) =
    SurfByteBuf.writeNullable(this, value, encodeFactory)

fun ByteBuf.writeNullableBoolean(value: Boolean?) =
    SurfByteBuf.writeNullable(this, value) { buf, bool -> buf.writeBoolean(bool) }

fun ByteBuf.writeNullableByte(value: Byte?) =
    SurfByteBuf.writeNullable(this, value) { buf, byte -> buf.writeByte(byte.toInt()) }

fun ByteBuf.writeNullableShort(value: Short?) =
    SurfByteBuf.writeNullable(this, value) { buf, short -> buf.writeShort(short.toInt()) }

@Deprecated("Use varInt instead", ReplaceWith("writeNullableVarInt(value)"))
fun ByteBuf.writeNullableInt(value: Int?) =
    SurfByteBuf.writeNullable(this, value) { buf, int -> buf.writeVarInt(int) }

fun ByteBuf.writeNullableVarInt(value: Int?) =
    SurfByteBuf.writeNullable(this, value) { buf, int -> buf.writeVarInt(int) }

fun ByteBuf.writeNullableLong(value: Long?) =
    SurfByteBuf.writeNullable(this, value) { buf, long -> buf.writeLong(long) }

fun ByteBuf.writeNullableFloat(value: Float?) =
    SurfByteBuf.writeNullable(this, value) { buf, float -> buf.writeFloat(float) }

fun ByteBuf.writeNullableDouble(value: Double?) =
    SurfByteBuf.writeNullable(this, value) { buf, double -> buf.writeDouble(double) }

fun ByteBuf.writeNullableChar(value: Char?) =
    SurfByteBuf.writeNullable(this, value) { buf, char -> buf.writeChar(char.code) }

fun ByteBuf.writeNullableString(value: String?) =
    SurfByteBuf.writeNullable(this, value) { buf, string -> SurfByteBuf.writeUtf(buf, string) }

fun ByteBuf.writeNullableUuid(value: UUID?) =
    SurfByteBuf.writeNullable(this, value) { buf, uuid -> SurfByteBuf.writeUuid(buf, uuid) }

fun <B : ByteBuf, T> B.writeOptional(optional: Optional<T>, encodeFactory: EncodeFactory<B, T>) =
    SurfByteBuf.writeOptional(this, optional, encodeFactory)

fun <B : ByteBuf, T : Any> B.readOptional(decodeFactory: DecodeFactory<B, T>) =
    SurfByteBuf.readOptional(this, decodeFactory)

fun ByteBuf.writeOptionalLong(optional: OptionalLong) =
    SurfByteBuf.writeOptionalLong(this, optional)

fun ByteBuf.readOptionalLong() = SurfByteBuf.readOptionalLong(this)
fun <B : ByteBuf> B.writeWithCount(count: Int, writer: (B) -> Unit) =
    SurfByteBuf.writeWithCount(this, count, writer)

fun <B : ByteBuf> B.writeWithCount(count: Int, writer: Consumer<B>) =
    Companion.writeWithCount(this, count, writer)

fun <B : ByteBuf> B.readWithCount(reader: (B) -> Unit) = SurfByteBuf.readWithCount(this, reader)
fun <B : ByteBuf> B.readWithCount(reader: Consumer<B>) = Companion.readWithCount(this, reader)

fun ByteBuf.wrap() = SurfByteBuf(this)
// endregion