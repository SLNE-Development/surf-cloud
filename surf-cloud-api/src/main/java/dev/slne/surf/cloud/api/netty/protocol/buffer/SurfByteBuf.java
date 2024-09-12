package dev.slne.surf.cloud.api.netty.protocol.buffer;

import dev.slne.surf.cloud.api.netty.protocol.buffer.codec.Codec;
import dev.slne.surf.cloud.api.netty.protocol.buffer.decoder.DecodeFactory;
import dev.slne.surf.cloud.api.netty.protocol.buffer.decoder.DecodeFactory.DecodeLongFactory;
import dev.slne.surf.cloud.api.netty.protocol.buffer.ecoder.EncodeFactory;
import dev.slne.surf.cloud.api.netty.protocol.buffer.ecoder.EncodeFactory.EncodeLongFactory;
import dev.slne.surf.cloud.api.netty.protocol.buffer.types.Utf8String;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unused"})
public class SurfByteBuf extends WrappedByteBuf {

  private static final byte NUMBER_BYTE = 0;
  private static final byte NUMBER_SHORT = 1;
  private static final byte NUMBER_INT = 2;
  private static final byte NUMBER_LONG = 3;
  private static final byte NUMBER_FLOAT = 4;
  private static final byte NUMBER_DOUBLE = 5;

  /**
   * Instantiates a new Surf byte buf.
   *
   * @param source the source
   */
  public SurfByteBuf(ByteBuf source) {
    super(source);
  }

  /**
   * Read key key.
   *
   * @param buf the buf
   * @return the key
   */
// region [Static methods]
  // region [Kyori Adventure]
  public static Key readKey(ByteBuf buf) {
    final @Subst("key:value") String keyString = readUtf(buf);
    return Key.key(keyString);
  }

  /**
   * Write key.
   *
   * @param buf the buf
   * @param key the key
   */
  public static void writeKey(ByteBuf buf, Key key) {
    writeUtf(buf, key.asString());
  }

  public static void writeSound(ByteBuf buf, Sound sound) {
    writeKey(buf, sound.name());
    writeEnum(buf, sound.source());
    buf.writeFloat(sound.volume());
    buf.writeFloat(sound.pitch());
    writeOptionalLong(buf, sound.seed());
  }

  public static Sound readSound(ByteBuf buf) {
    return Sound.sound()
        .type(readKey(buf))
        .source(readEnum(buf, Sound.Source.class))
        .volume(buf.readFloat())
        .pitch(buf.readFloat())
        .seed(readOptionalLong(buf))
        .build();
  }

  public static Component readComponent(ByteBuf buf) {
//    return BinaryComponentSerializer.deserializeComponent(buf);
    final class Holder {
      final static GsonComponentSerializer serializer = GsonComponentSerializer.gson();
    }

    return Holder.serializer.deserialize(readUtf(buf));
  }

  /**
   * Write component.
   *
   * @param buf       the buf
   * @param component the component
   */
  public static void writeComponent(ByteBuf buf, Component component) {
//    BinaryComponentSerializer.serializeComponent(component, buf);
    final class Holder {

      final static GsonComponentSerializer serializer = GsonComponentSerializer.gson();
    }

    writeUtf(buf, Holder.serializer.serialize(component));
  }
  // endregion

  /**
   * Write number.
   *
   * @param buf    the buf
   * @param number the number
   */
// region [Java Types]
  public static void writeNumber(ByteBuf buf, Number number) {
    switch (number) {
      case Byte byteValue -> {
        buf.writeByte(NUMBER_BYTE);
        buf.writeByte(byteValue);
      }
      case Short shortValue -> {
        buf.writeByte(NUMBER_SHORT);
        buf.writeShort(shortValue);
      }
      case Integer intValue -> {
        buf.writeByte(NUMBER_INT);
        buf.writeInt(intValue);
      }
      case Long longValue -> {
        buf.writeByte(NUMBER_LONG);
        buf.writeLong(longValue);
      }
      case Float floatValue -> {
        buf.writeByte(NUMBER_FLOAT);
        buf.writeFloat(floatValue);
      }
      case Double doubleValue -> {
        buf.writeByte(NUMBER_DOUBLE);
        buf.writeDouble(doubleValue);
      }
      default -> throw new EncoderException("Unsupported number type: " + number.getClass());
    }
  }

  /**
   * Read number number.
   *
   * @param buf the buf
   * @return the number
   */
  public static Number readNumber(ByteBuf buf) {
    return switch (buf.readByte()) {
      case NUMBER_BYTE -> buf.readByte();
      case NUMBER_SHORT -> buf.readShort();
      case NUMBER_INT -> buf.readInt();
      case NUMBER_LONG -> buf.readLong();
      case NUMBER_FLOAT -> buf.readFloat();
      case NUMBER_DOUBLE -> buf.readDouble();
      default -> throw new DecoderException("Unknown number type");
    };
  }

  /**
   * Read collection c.
   *
   * @param <T>               the type parameter
   * @param <C>               the type parameter
   * @param <B>               the type parameter
   * @param buf               the buf
   * @param collectionFactory the collection factory
   * @param decodeFactory     the decode factory
   * @return the c
   */
  public static <T, C extends Collection<T>, B extends ByteBuf> C readCollection(
      B buf,
      IntFunction<C> collectionFactory,
      DecodeFactory<B, T> decodeFactory
  ) {
    final int size = buf.readInt();
    final C collection = collectionFactory.apply(size);

    for (int i = 0; i < size; i++) {
      collection.add(decodeFactory.decode(buf));
    }

    return collection;
  }

  /**
   * Write collection.
   *
   * @param <T>           the type parameter
   * @param <B>           the type parameter
   * @param buf           the buf
   * @param collection    the collection
   * @param encodeFactory the encode factory
   */
  public static <T, B extends ByteBuf> void writeCollection(
      B buf,
      Collection<T> collection,
      EncodeFactory<? super B, T> encodeFactory
  ) {
    buf.writeInt(collection.size());

    for (final T element : collection) {
      encodeFactory.encode(buf, element);
    }
  }

  /**
   * Read list object array list.
   *
   * @param <T>           the type parameter
   * @param <B>           the type parameter
   * @param buf           the buf
   * @param decodeFactory the decode factory
   * @return the object array list
   */
  public static <T, B extends ByteBuf> ObjectArrayList<T> readList(
      B buf,
      DecodeFactory<B, T> decodeFactory
  ) {
    return readCollection(buf, ObjectArrayList::new, decodeFactory);
  }

  /**
   * Read map m.
   *
   * @param <K>                the type parameter
   * @param <V>                the type parameter
   * @param <M>                the type parameter
   * @param <B>                the type parameter
   * @param buf                the buf
   * @param mapFactory         the map factory
   * @param keyDecodeFactory   the key decode factory
   * @param valueDecodeFactory the value decode factory
   * @return the m
   */
  public static <K, V, M extends Map<K, V>, B extends ByteBuf> M readMap(
      B buf,
      IntFunction<M> mapFactory,
      DecodeFactory<? super B, K> keyDecodeFactory,
      DecodeFactory<? super B, V> valueDecodeFactory
  ) {
    final int size = buf.readInt();
    final M map = mapFactory.apply(size);

    for (int i = 0; i < size; i++) {
      final K key = keyDecodeFactory.decode(buf);
      final V value = valueDecodeFactory.decode(buf);

      map.put(key, value);
    }

    return map;
  }

  /**
   * Read map object 2 object open hash map.
   *
   * @param <K>                the type parameter
   * @param <V>                the type parameter
   * @param <B>                the type parameter
   * @param buf                the buf
   * @param keyDecodeFactory   the key decode factory
   * @param valueDecodeFactory the value decode factory
   * @return the object 2 object open hash map
   */
  public static <K, V, B extends ByteBuf> Object2ObjectOpenHashMap<K, V> readMap(
      B buf,
      DecodeFactory<? super B, K> keyDecodeFactory,
      DecodeFactory<? super B, V> valueDecodeFactory
  ) {
    return readMap(buf, Object2ObjectOpenHashMap::new, keyDecodeFactory, valueDecodeFactory);
  }

  /**
   * Write map.
   *
   * @param <K>                the type parameter
   * @param <V>                the type parameter
   * @param <B>                the type parameter
   * @param buf                the buf
   * @param map                the map
   * @param keyEncodeFactory   the key encode factory
   * @param valueEncodeFactory the value encode factory
   */
  public static <K, V, B extends ByteBuf> void writeMap(
      B buf,
      Map<K, V> map,
      EncodeFactory<? super B, K> keyEncodeFactory,
      EncodeFactory<? super B, V> valueEncodeFactory
  ) {
    buf.writeInt(map.size());

    for (final Map.Entry<K, V> entry : map.entrySet()) {
      keyEncodeFactory.encode(buf, entry.getKey());
      valueEncodeFactory.encode(buf, entry.getValue());
    }
  }

  /**
   * Read enum set enum set.
   *
   * @param <E>       the type parameter
   * @param buf       the buf
   * @param enumClass the enum class
   * @return the enum set
   */
  public static <E extends Enum<E>> EnumSet<E> readEnumSet(
      ByteBuf buf,
      Class<E> enumClass
  ) {
    final E[] values = enumClass.getEnumConstants();
    final BitSet bitSet = readFixedBitSet(buf, values.length);
    final EnumSet<E> enumSet = EnumSet.noneOf(enumClass);

    for (int i = 0; i < values.length; i++) {
      if (bitSet.get(i)) {
        enumSet.add(values[i]);
      }
    }

    return enumSet;
  }

  /**
   * Write enum set.
   *
   * @param <E>       the type parameter
   * @param buf       the buf
   * @param enumSet   the enum set
   * @param enumClass the enum class
   */
  public static <E extends Enum<E>> void writeEnumSet(
      ByteBuf buf,
      EnumSet<E> enumSet,
      Class<E> enumClass
  ) {
    final E[] values = enumClass.getEnumConstants();
    final BitSet bitSet = new BitSet(values.length);

    for (int i = 0; i < values.length; i++) {
      bitSet.set(i, enumSet.contains(values[i]));
    }

    writeFixedBitSet(buf, bitSet, values.length);
  }

  /**
   * Read enum e.
   *
   * @param <E>       the type parameter
   * @param buf       the buf
   * @param enumClass the enum class
   * @return the e
   */
  public static <E extends Enum<E>> E readEnum(ByteBuf buf, Class<E> enumClass) {
    return enumClass.getEnumConstants()[buf.readInt()];
  }

  /**
   * Write enum.
   *
   * @param <E>   the type parameter
   * @param buf   the buf
   * @param value the value
   */
  public static <E extends Enum<E>> void writeEnum(ByteBuf buf, E value) {
    buf.writeInt(value.ordinal());
  }

  /**
   * Write uuid.
   *
   * @param buf  the buf
   * @param uuid the uuid
   */
  public static void writeUuid(ByteBuf buf, UUID uuid) {
    buf.writeLong(uuid.getMostSignificantBits());
    buf.writeLong(uuid.getLeastSignificantBits());
  }

  /**
   * Read uuid uuid.
   *
   * @param buf the buf
   * @return the uuid
   */
  public static UUID readUuid(ByteBuf buf) {
    return new UUID(buf.readLong(), buf.readLong());
  }

  /**
   * Write long array b.
   *
   * @param <B>   the type parameter
   * @param buf   the buf
   * @param array the array
   * @return the b
   */
  public static <B extends ByteBuf> B writeLongArray(B buf, long[] array) {
    buf.writeInt(array.length);

    for (final long longValue : array) {
      buf.writeLong(longValue);
    }

    return buf;
  }

  /**
   * Read long array long [ ].
   *
   * @param buf the buf
   * @return the long [ ]
   */
  public static long[] readLongArray(ByteBuf buf) {
    return readLongArray(buf, null);
  }

  /**
   * Read long array long [ ].
   *
   * @param buf     the buf
   * @param toArray the to array
   * @return the long [ ]
   */
  public static long[] readLongArray(ByteBuf buf, long @Nullable [] toArray) {
    return readLongArray(buf, toArray, buf.readableBytes() / Byte.SIZE);
  }

  /**
   * Read long array long [ ].
   *
   * @param buf     the buf
   * @param toArray the to array
   * @param maxSize the max size
   * @return the long [ ]
   */
  public static long[] readLongArray(ByteBuf buf, long @Nullable [] toArray, int maxSize) {
    final int size = buf.readInt();

    if (toArray == null || toArray.length != size) {
      if (size > maxSize) {
        throw new DecoderException(
            "LongArray with size " + size + " is bigger than allowed " + maxSize);
      }

      toArray = new long[size];
    }

    for (int i = 0; i < toArray.length; ++i) {
      toArray[i] = buf.readLong();
    }

    return toArray;
  }

  /**
   * Read bit set bit set.
   *
   * @param buf the buf
   * @return the bit set
   */
  public static BitSet readBitSet(ByteBuf buf) {
    return BitSet.valueOf(readLongArray(buf));
  }

  /**
   * Write bit set.
   *
   * @param buf    the buf
   * @param bitSet the bit set
   */
  public static void writeBitSet(ByteBuf buf, BitSet bitSet) {
    writeLongArray(buf, bitSet.toLongArray());
  }

  /**
   * Read fixed bit set bit set.
   *
   * @param buf  the buf
   * @param size the size
   * @return the bit set
   */
  public static BitSet readFixedBitSet(ByteBuf buf, int size) {
    final byte[] bytes = new byte[positiveCeilDiv(size, Byte.SIZE)];
    buf.readBytes(bytes);

    return BitSet.valueOf(bytes);
  }

  /**
   * Write fixed bit set.
   *
   * @param buf    the buf
   * @param bitSet the bit set
   * @param size   the size
   */
  public static void writeFixedBitSet(ByteBuf buf, BitSet bitSet, int size) {
    final int bitSetSize = bitSet.length();

    if (bitSetSize > size) {
      throw new EncoderException(
          "BitSet is larger than expected size (" + bitSetSize + ">" + size + ")");
    }

    final byte[] bitSetBytes = bitSet.toByteArray();
    buf.writeBytes(Arrays.copyOf(bitSetBytes, positiveCeilDiv(size, Byte.SIZE)));
  }

  /**
   * Read utf string.
   *
   * @param buf the buf
   * @return the string
   */
  public static String readUtf(ByteBuf buf) {
    return readUtf(buf, Integer.MAX_VALUE);
  }

  /**
   * Read utf string.
   *
   * @param buf       the buf
   * @param maxLength the max length
   * @return the string
   */
  public static String readUtf(ByteBuf buf, int maxLength) {
    return Utf8String.read(buf, maxLength);
  }

  /**
   * Write utf.
   *
   * @param buf    the buf
   * @param string the string
   */
  public static void writeUtf(ByteBuf buf, String string) {
    writeUtf(buf, string, Integer.MAX_VALUE);
  }

  /**
   * Write utf.
   *
   * @param buf       the buf
   * @param string    the string
   * @param maxLength the max length
   */
  public static void writeUtf(ByteBuf buf, String string, int maxLength) {
    Utf8String.write(buf, string, maxLength);
  }
  // endregion

  /**
   * Write with count.
   *
   * @param <B>    the type parameter
   * @param buf    the buf
   * @param count  the count
   * @param writer the writer
   */
// region [Helper methods]
  public static <B extends ByteBuf> void writeWithCount(B buf, int count, Consumer<B> writer) {
    buf.writeInt(count);

    for (int i = 0; i < count; i++) {
      writer.accept(buf);
    }
  }

  /**
   * Read with count.
   *
   * @param <B>    the type parameter
   * @param <T>    the type parameter
   * @param buf    the buf
   * @param reader the reader
   */
  public static <B extends ByteBuf, T> void readWithCount(B buf, Consumer<B> reader) {
    final int count = buf.readInt();

    for (int i = 0; i < count; i++) {
      reader.accept(buf);
    }
  }

  /**
   * Read nullable t.
   *
   * @param <T>           the type parameter
   * @param <B>           the type parameter
   * @param buf           the buf
   * @param decodeFactory the decode factory
   * @return the t
   */
  public static <T, B extends ByteBuf> T readNullable(
      B buf,
      DecodeFactory<? super B, T> decodeFactory
  ) {
    return buf.readBoolean() ? decodeFactory.decode(buf) : null;
  }

  /**
   * Write nullable.
   *
   * @param <T>           the type parameter
   * @param <B>           the type parameter
   * @param buf           the buf
   * @param value         the value
   * @param encodeFactory the encode factory
   */
  public static <T, B extends ByteBuf> void writeNullable(
      B buf,
      @Nullable T value,
      EncodeFactory<? super B, T> encodeFactory
  ) {
    buf.writeBoolean(value != null);

    if (value != null) {
      encodeFactory.encode(buf, value);
    }
  }

  /**
   * Write optional.
   *
   * @param <T>           the type parameter
   * @param <B>           the type parameter
   * @param buf           the buf
   * @param optional      the optional
   * @param encodeFactory the encode factory
   */
  public static <T, B extends ByteBuf> void writeOptional(
      B buf,
      Optional<T> optional,
      EncodeFactory<? super B, T> encodeFactory
  ) {
    buf.writeBoolean(optional.isPresent());

    optional.ifPresent(value -> encodeFactory.encode(buf, value));
  }

  /**
   * Read optional optional.
   *
   * @param <T>           the type parameter
   * @param <B>           the type parameter
   * @param buf           the buf
   * @param decodeFactory the decode factory
   * @return the optional
   */
  public static <T, B extends ByteBuf> Optional<T> readOptional(
      B buf,
      DecodeFactory<? super B, T> decodeFactory
  ) {
    return buf.readBoolean() ? Optional.of(decodeFactory.decode(buf)) : Optional.empty();
  }

  public static <T, B extends ByteBuf> void writeOptionalLong(
      B buf,
      OptionalLong optional,
      EncodeLongFactory<? super B> encodeFactory
  ) {
    buf.writeBoolean(optional.isPresent());

    optional.ifPresent(value -> encodeFactory.encodeLong(buf, value));
  }

  public static <B extends ByteBuf> OptionalLong readOptionalLong(
      B buf,
      DecodeLongFactory<? super B> decodeFactory
  ) {
    return buf.readBoolean() ? OptionalLong.of(decodeFactory.decodeLong(buf))
        : OptionalLong.empty();
  }

  public static <T, B extends ByteBuf> void writeOptionalLong(B buf, OptionalLong optional) {
    writeOptionalLong(buf, optional, ByteBuf::writeLong);
  }

  public static <B extends ByteBuf> OptionalLong readOptionalLong(B buf) {
    return readOptionalLong(buf, ByteBuf::readLong);
  }
  // endregion

  // endregion

  /**
   * Positive ceil div int.
   *
   * @param a the a
   * @param b the b
   * @return the int
   */
// region [Helper methods]
  @SuppressWarnings("SameParameterValue")
  private static int positiveCeilDiv(int a, int b) {
    return -Math.floorDiv(-a, b);
  }

  // endregion

// region [Instance methods]

  public <T, DX extends Throwable, EX extends Throwable> T readWithCodec(Codec<T, DX, EX> codec)
      throws DX {
    return codec.decode(this);
  }

  public <T, DX extends Throwable, EX extends Throwable> void writeWithCodec(Codec<T, DX, EX> codec,
      T value) throws EX {
    codec.encode(this, value);
  }

  /**
   * Read key key.
   *
   * @return the key
   */
  public Key readKey() {
    return readKey(this);
  }

  /**
   * Write key.
   *
   * @param key the key
   */
  public void writeKey(Key key) {
    writeUtf(key.asString());
  }

  public void writeSound(Sound sound) {
    writeSound(this, sound);
  }

  public Sound readSound() {
    return readSound(this);
  }

  public Component readComponent() {
    return readComponent(this);
  }

  /**
   * Write component.
   *
   * @param component the component
   */
  public void writeComponent(Component component) {
    writeComponent(this, component);
  }

  /**
   * Read collection c.
   *
   * @param <T>               the type parameter
   * @param <C>               the type parameter
   * @param collectionFactory the collection factory
   * @param decodeFactory     the decode factory
   * @return the c
   */
  public <T, C extends Collection<T>> C readCollection(
      IntFunction<C> collectionFactory,
      DecodeFactory<? super SurfByteBuf, T> decodeFactory
  ) {
    return readCollection(this, collectionFactory, decodeFactory);
  }

  /**
   * Write collection.
   *
   * @param <T>           the type parameter
   * @param collection    the collection
   * @param encodeFactory the encode factory
   */
  public <T> void writeCollection(
      Collection<T> collection,
      EncodeFactory<? super SurfByteBuf, T> encodeFactory
  ) {
    writeCollection(this, collection, encodeFactory);
  }

  /**
   * Read list object array list.
   *
   * @param <T>           the type parameter
   * @param decodeFactory the decode factory
   * @return the object array list
   */
  public <T> ObjectArrayList<T> readList(DecodeFactory<? super SurfByteBuf, T> decodeFactory) {
    return readList(this, decodeFactory);
  }

  /**
   * Read map m.
   *
   * @param <K>                the type parameter
   * @param <V>                the type parameter
   * @param <M>                the type parameter
   * @param mapFactory         the map factory
   * @param keyDecodeFactory   the key decode factory
   * @param valueDecodeFactory the value decode factory
   * @return the m
   */
  public <K, V, M extends Map<K, V>> M readMap(
      IntFunction<M> mapFactory,
      DecodeFactory<? super SurfByteBuf, K> keyDecodeFactory,
      DecodeFactory<? super SurfByteBuf, V> valueDecodeFactory
  ) {
    return readMap(this, mapFactory, keyDecodeFactory, valueDecodeFactory);
  }

  /**
   * Read map object 2 object open hash map.
   *
   * @param <K>                the type parameter
   * @param <V>                the type parameter
   * @param keyDecodeFactory   the key decode factory
   * @param valueDecodeFactory the value decode factory
   * @return the object 2 object open hash map
   */
  public <K, V> Object2ObjectOpenHashMap<K, V> readMap(
      DecodeFactory<? super SurfByteBuf, K> keyDecodeFactory,
      DecodeFactory<? super SurfByteBuf, V> valueDecodeFactory
  ) {
    return readMap(this, keyDecodeFactory, valueDecodeFactory);
  }

  /**
   * Write map.
   *
   * @param <K>                the type parameter
   * @param <V>                the type parameter
   * @param map                the map
   * @param keyEncodeFactory   the key encode factory
   * @param valueEncodeFactory the value encode factory
   */
  public <K, V> void writeMap(
      Map<K, V> map,
      EncodeFactory<? super SurfByteBuf, K> keyEncodeFactory,
      EncodeFactory<? super SurfByteBuf, V> valueEncodeFactory
  ) {
    writeMap(this, map, keyEncodeFactory, valueEncodeFactory);
  }

  /**
   * Write enum set.
   *
   * @param <E>       the type parameter
   * @param enumSet   the enum set
   * @param enumClass the enum class
   */
  public <E extends Enum<E>> void writeEnumSet(EnumSet<E> enumSet, Class<E> enumClass) {
    writeEnumSet(this, enumSet, enumClass);
  }

  /**
   * Read enum set enum set.
   *
   * @param <E>       the type parameter
   * @param enumClass the enum class
   * @return the enum set
   */
  public <E extends Enum<E>> EnumSet<E> readEnumSet(Class<E> enumClass) {
    return readEnumSet(this, enumClass);
  }

  /**
   * Read enum e.
   *
   * @param <E>       the type parameter
   * @param enumClass the enum class
   * @return the e
   */
  public <E extends Enum<E>> E readEnum(Class<E> enumClass) {
    return readEnum(this, enumClass);
  }

  /**
   * Write enum.
   *
   * @param <E>   the type parameter
   * @param value the value
   */
  public <E extends Enum<E>> void writeEnum(E value) {
    writeEnum(this, value);
  }

  /**
   * Write optional.
   *
   * @param <T>           the type parameter
   * @param optional      the optional
   * @param encodeFactory the encode factory
   */
  public <T> void writeOptional(Optional<T> optional,
      EncodeFactory<? super SurfByteBuf, T> encodeFactory) {
    writeOptional(this, optional, encodeFactory);
  }

  /**
   * Read optional optional.
   *
   * @param <T>           the type parameter
   * @param decodeFactory the decode factory
   * @return the optional
   */
  public <T> Optional<T> readOptional(DecodeFactory<? super SurfByteBuf, T> decodeFactory) {
    return readOptional(this, decodeFactory);
  }

  public void writeOptionalLong(OptionalLong optional) {
    writeOptionalLong(this, optional);
  }

  public OptionalLong readOptionalLong() {
    return readOptionalLong(this);
  }

  public void writeOptionalLong(
      EncodeLongFactory<? super SurfByteBuf> encodeFactory,
      OptionalLong optional
  ) {
    writeOptionalLong(this, optional, encodeFactory);
  }

  public OptionalLong readOptionalLong(DecodeLongFactory<? super SurfByteBuf> decodeFactory) {
    return readOptionalLong(this, decodeFactory);
  }

  /**
   * Read nullable @ nullable t.
   *
   * @param <T>           the type parameter
   * @param decodeFactory the decode factory
   * @return the @ nullable t
   */
  public <T> @Nullable T readNullable(DecodeFactory<? super SurfByteBuf, T> decodeFactory) {
    return readNullable(this, decodeFactory);
  }

  /**
   * Write nullable.
   *
   * @param <T>           the type parameter
   * @param value         the value
   * @param encodeFactory the encode factory
   */
  public <T> void writeNullable(@Nullable T value,
      EncodeFactory<? super SurfByteBuf, T> encodeFactory) {
    writeNullable(this, value, encodeFactory);
  }

  public void writeNullable(String value) {
    writeNullable(this, value, SurfByteBuf::writeString);
  }

  public void writeNullable(Integer value) {
    writeNullable(this, value, SurfByteBuf::writeInt);
  }

  public void writeNullable(Long value) {
    writeNullable(this, value, SurfByteBuf::writeLong);
  }

  public void writeNullable(Float value) {
    writeNullable(this, value, SurfByteBuf::writeFloat);
  }

  public void writeNullable(Double value) {
    writeNullable(this, value, SurfByteBuf::writeDouble);
  }

  public void writeNullable(Boolean value) {
    writeNullable(this, value, SurfByteBuf::writeBoolean);
  }

  public void writeNullable(UUID value) {
    writeNullable(this, value, (buffer, value1) -> buffer.writeUuid(value1));
  }

  /**
   * Write uuid.
   *
   * @param uuid the uuid
   */
  public void writeUuid(UUID uuid) {
    writeUuid(this, uuid);
  }

  /**
   * Read uuid uuid.
   *
   * @return the uuid
   */
  public UUID readUuid() {
    return readUuid(this);
  }

  /**
   * Write long array surf byte buf.
   *
   * @param array the array
   * @return the surf byte buf
   */
  public SurfByteBuf writeLongArray(long[] array) {
    return writeLongArray(this, array);
  }

  /**
   * Read long array long [ ].
   *
   * @return the long [ ]
   */
  public long[] readLongArray() {
    return readLongArray(this);
  }

  /**
   * Read long array long [ ].
   *
   * @param toArray the to array
   * @return the long [ ]
   */
  public long[] readLongArray(long @Nullable [] toArray) {
    return readLongArray(this, toArray);
  }

  /**
   * Read long array long [ ].
   *
   * @param toArray the to array
   * @param maxSize the max size
   * @return the long [ ]
   */
  public long[] readLongArray(long @Nullable [] toArray, int maxSize) {
    return readLongArray(this, toArray, maxSize);
  }

  /**
   * Read bit set bit set.
   *
   * @return the bit set
   */
  public BitSet readBitSet() {
    return readBitSet(this);
  }

  /**
   * Write bit set.
   *
   * @param bitSet the bit set
   */
  public void writeBitSet(BitSet bitSet) {
    writeBitSet(this, bitSet);
  }

  /**
   * Read fixed bit set bit set.
   *
   * @param size the size
   * @return the bit set
   */
  public BitSet readFixedBitSet(int size) {
    return readFixedBitSet(this, size);
  }

  /**
   * Write fixed bit set.
   *
   * @param bitSet the bit set
   * @param size   the size
   */
  public void writeFixedBitSet(BitSet bitSet, int size) {
    writeFixedBitSet(this, bitSet, size);
  }

  /**
   * Read utf string.
   *
   * @return the string
   */
  public String readUtf() {
    return readUtf(this);
  }

  /**
   * Read utf string.
   *
   * @param maxLength the max length
   * @return the string
   */
  public String readUtf(int maxLength) {
    return readUtf(this, maxLength);
  }

  /**
   * Write utf.
   *
   * @param string the string
   */
  public void writeUtf(String string) {
    writeUtf(this, string);
  }
  // endregion

  /**
   * Write utf.
   *
   * @param string    the string
   * @param maxLength the max length
   */
  public void writeUtf(String string, int maxLength) {
    writeUtf(this, string, maxLength);
  }
  // endregion

  /**
   * Read string string.
   *
   * @return the string
   */
  public String readString() {
    return readUtf();
  }

  /**
   * Read string string.
   *
   * @param maxLength the max length
   * @return the string
   */
  public String readString(int maxLength) {
    return readUtf(maxLength);
  }

  /**
   * Write string.
   *
   * @param string the string
   */
  public void writeString(String string) {
    writeUtf(string);
  }

  /**
   * Write string.
   *
   * @param string    the string
   * @param maxLength the max length
   */
  public void writeString(String string, int maxLength) {
    writeUtf(string, maxLength);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
