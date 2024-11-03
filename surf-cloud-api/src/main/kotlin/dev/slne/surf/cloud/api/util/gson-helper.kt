package dev.slne.surf.cloud.api.util

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import org.apache.commons.lang3.StringUtils
import java.io.IOException
import java.io.Reader
import java.io.StringReader
import java.io.StringWriter
import java.math.BigDecimal
import java.math.BigInteger

private val GSON: Gson = GsonBuilder().create()

fun JsonObject.isStringValue(element: String?): Boolean =
    isValidPrimitive(element) && getAsJsonPrimitive(element).isString

fun JsonElement.isStringValue(): Boolean = isJsonPrimitive && asJsonPrimitive.isString

fun JsonObject.isNumberValue(element: String?): Boolean =
    isValidPrimitive(element) && getAsJsonPrimitive(element).isNumber

fun JsonElement.isNumberValue(): Boolean = isJsonPrimitive && asJsonPrimitive.isNumber

fun JsonObject.isBooleanValue(element: String?): Boolean =
    isValidPrimitive(element) && getAsJsonPrimitive(element).isBoolean

fun JsonElement.isBooleanValue(): Boolean = isJsonPrimitive && asJsonPrimitive.isBoolean

fun JsonObject.isArrayNode(element: String?): Boolean =
    isValidNode(element) && this[element].isJsonArray

fun JsonObject.isObjectNode(element: String?): Boolean =
    isValidNode(element) && this[element].isJsonObject

fun JsonObject.isValidPrimitive(element: String?): Boolean =
    isValidNode(element) && this[element].isJsonPrimitive

fun JsonObject?.isValidNode(element: String?): Boolean = this?.get(element) != null

fun JsonObject.getNonNull(name: String): JsonElement =
    this[name]?.takeIf { !it.isJsonNull }
        ?: throw JsonSyntaxException("Missing field $name")

fun JsonElement.convertToString(name: String): String =
    if (isJsonPrimitive) asString
    else throw JsonSyntaxException("Expected $name to be a string, was ${getType()}")

fun JsonObject.getAsString(element: String): String =
    if (has(element)) this[element].convertToString(element)
    else throw JsonSyntaxException("Missing $element, expected to find a string")

fun JsonObject.getAsString(element: String, defaultStr: String?): String? =
    if (has(element)) this[element]?.convertToString(element) else defaultStr

fun JsonElement.convertToBoolean(name: String): Boolean =
    if (isJsonPrimitive) asBoolean
    else throw JsonSyntaxException("Expected $name to be a Boolean, was ${getType()}")

fun JsonObject.getAsBoolean(element: String): Boolean =
    if (has(element)) this[element].convertToBoolean(element)
    else throw JsonSyntaxException("Missing $element, expected to find a Boolean")

fun JsonObject.getAsBoolean(element: String, defaultBoolean: Boolean): Boolean =
    if (has(element)) this[element].convertToBoolean(element) else defaultBoolean

fun JsonElement.convertToNumber(name: String): Number =
    if (isJsonPrimitive && asJsonPrimitive.isNumber) asNumber
    else throw JsonSyntaxException("Expected $name to be a number, was ${getType()}")

inline fun <reified T : Number> JsonObject.getAsNumber(element: String): T =
    if (has(element)) convertToNumber(element) as T
    else throw JsonSyntaxException("Missing $element, expected to find a number")

inline fun <reified T : Number> JsonObject.getAsNumber(element: String, defaultValue: T): T =
    if (has(element)) convertToNumber(element) as T else defaultValue

fun JsonElement.convertToBigDecimal(name: String): BigDecimal =
    if (isJsonPrimitive && asJsonPrimitive.isNumber) asBigDecimal
    else throw JsonSyntaxException("Expected $name to be a BigDecimal, was ${getType()}")

fun JsonObject.getAsBigDecimal(element: String): BigDecimal =
    if (has(element)) this[element].convertToBigDecimal(element)
    else throw JsonSyntaxException("Missing $element, expected to find a BigDecimal")

fun JsonObject.getAsBigDecimal(element: String, defaultBigDecimal: BigDecimal): BigDecimal =
    if (has(element)) this[element].convertToBigDecimal(element) else defaultBigDecimal

fun JsonElement.convertToBigInteger(name: String): BigInteger =
    if (isJsonPrimitive && asJsonPrimitive.isNumber) asBigInteger
    else throw JsonSyntaxException("Expected $name to be a BigInteger, was ${getType()}")

fun JsonObject.getAsBigInteger(element: String): BigInteger =
    if (has(element)) this[element].convertToBigInteger(element)
    else throw JsonSyntaxException("Missing $element, expected to find a BigInteger")

fun JsonObject.getAsBigInteger(element: String, defaultBigInteger: BigInteger): BigInteger =
    if (has(element)) this[element].convertToBigInteger(element) else defaultBigInteger

fun JsonElement.convertToLong(name: String): Long =
    if (isJsonPrimitive && asJsonPrimitive.isNumber) asLong
    else throw JsonSyntaxException("Expected $name to be a Long, was ${getType()}")

fun JsonObject.getAsLong(element: String): Long =
    if (has(element)) this[element].convertToLong(element)
    else throw JsonSyntaxException("Missing $element, expected to find a Long")

fun JsonObject.getAsLong(element: String, defaultLong: Long): Long =
    if (has(element)) this[element].convertToLong(element) else defaultLong

fun JsonElement.convertToInt(name: String): Int =
    if (isJsonPrimitive && asJsonPrimitive.isNumber) asInt
    else throw JsonSyntaxException("Expected $name to be an Int, was ${getType()}")

fun JsonObject.getAsInt(element: String): Int =
    if (has(element)) this[element].convertToInt(element)
    else throw JsonSyntaxException("Missing $element, expected to find an Int")

fun JsonObject.getAsInt(element: String, defaultInt: Int): Int =
    if (has(element)) this[element].convertToInt(element) else defaultInt

fun JsonElement.convertToShort(name: String): Short =
    if (isJsonPrimitive && asJsonPrimitive.isNumber) asShort
    else throw JsonSyntaxException("Expected $name to be a Short, was ${getType()}")

fun JsonObject.getAsShort(element: String): Short =
    if (has(element)) this[element].convertToShort(element)
    else throw JsonSyntaxException("Missing $element, expected to find a Short")

fun JsonObject.getAsShort(element: String, defaultShort: Short): Short =
    if (has(element)) this[element].convertToShort(element) else defaultShort

fun JsonElement.convertToByte(name: String): Byte =
    if (isJsonPrimitive && asJsonPrimitive.isNumber) asByte
    else throw JsonSyntaxException("Expected $name to be a Byte, was ${getType()}")

fun JsonObject.getAsByte(element: String): Byte =
    if (has(element)) this[element].convertToByte(element)
    else throw JsonSyntaxException("Missing $element, expected to find a Byte")

fun JsonObject.getAsByte(element: String, defaultByte: Byte): Byte =
    if (has(element)) this[element].convertToByte(element) else defaultByte

fun JsonElement.convertToJsonObject(name: String): JsonObject =
    if (isJsonObject) asJsonObject
    else throw JsonSyntaxException("Expected $name to be a JsonObject, was ${getType()}")

fun JsonObject.getAsJsonObject(element: String): JsonObject =
    if (has(element)) this[element].convertToJsonObject(element)
    else throw JsonSyntaxException("Missing $element, expected to find a JsonObject")

fun JsonObject.getAsJsonObject(element: String, defaultObject: JsonObject?): JsonObject? =
    if (has(element)) this[element].convertToJsonObject(element) else defaultObject

fun JsonElement.convertToJsonArray(name: String): JsonArray =
    if (isJsonArray) asJsonArray
    else throw JsonSyntaxException("Expected $name to be a JsonArray, was ${getType()}")

fun JsonObject.getAsJsonArray(element: String): JsonArray =
    if (has(element)) this[element].convertToJsonArray(element)
    else throw JsonSyntaxException("Missing $element, expected to find a JsonArray")

fun JsonObject.getAsJsonArray(element: String, defaultArray: JsonArray?): JsonArray? =
    if (has(element)) this[element].convertToJsonArray(element) else defaultArray

fun JsonElement?.getType(): String {
    val string = StringUtils.abbreviateMiddle(toString(), "...", 10)
    return when {
        this == null -> "null (missing)"
        isJsonNull -> "null (json)"
        isJsonArray -> "an array ($string)"
        isJsonObject -> "an object ($string)"
        isJsonPrimitive -> {
            val jsonPrimitive = asJsonPrimitive
            when {
                jsonPrimitive.isNumber -> "a number ($string)"
                jsonPrimitive.isBoolean -> "a boolean ($string)"
                else -> string
            }
        }

        else -> string
    }
}

fun <T> fromNullableJson(gson: Gson, reader: Reader?, type: Class<T>, lenient: Boolean): T? =
    try {
        JsonReader(reader).apply { isLenient = lenient }.use {
            gson.getAdapter(type).read(it)
        }
    } catch (e: IOException) {
        throw JsonParseException(e)
    }

fun <T> fromJson(gson: Gson, reader: Reader?, type: Class<T>, lenient: Boolean): T =
    fromNullableJson(gson, reader, type, lenient)
        ?: throw JsonParseException("JSON data was null or empty")

fun <T> fromNullableJson(gson: Gson, content: String?, type: Class<T>, lenient: Boolean): T? =
    fromNullableJson(gson, StringReader(content), type, lenient)

fun <T> fromJson(gson: Gson, content: String?, type: Class<T>, lenient: Boolean): T =
    fromJson(gson, StringReader(content), type, lenient)

fun <T> fromNullableJson(
    gson: Gson,
    reader: Reader?,
    typeToken: TypeToken<T>,
    lenient: Boolean
): T? =
    try {
        JsonReader(reader).apply { isLenient = lenient }.use {
            gson.getAdapter(typeToken).read(it)
        }
    } catch (e: IOException) {
        throw JsonParseException(e)
    }

fun <T> fromJson(gson: Gson, reader: Reader?, typeToken: TypeToken<T>, lenient: Boolean): T =
    fromNullableJson(gson, reader, typeToken, lenient)
        ?: throw JsonParseException("JSON data was null or empty")

fun <T> fromNullableJson(
    gson: Gson,
    content: String?,
    typeToken: TypeToken<T>,
    lenient: Boolean
): T? =
    fromNullableJson(gson, StringReader(content), typeToken, lenient)

fun <T> fromJson(gson: Gson, content: String?, typeToken: TypeToken<T>, lenient: Boolean): T =
    fromJson(gson, StringReader(content), typeToken, lenient)

fun <T> fromJson(gson: Gson, reader: Reader?, typeToken: TypeToken<T>): T =
    fromJson(gson, reader, typeToken, false)

fun <T> fromNullableJson(gson: Gson, content: String?, typeToken: TypeToken<T>): T? =
    fromNullableJson(gson, content, typeToken, false)

fun <T> fromJson(gson: Gson, reader: Reader?, type: Class<T>): T =
    fromJson(gson, reader, type, false)

fun <T> fromJson(gson: Gson, content: String?, type: Class<T>): T =
    fromJson(gson, content, type, false)

inline fun <reified T> fromJson(gson: Gson, content: String?): T =
    fromJson(gson, content, T::class.java)

@JvmOverloads
fun parse(content: String?, lenient: Boolean = false): JsonObject =
    parse(StringReader(content), lenient)

@JvmOverloads
fun parse(reader: Reader?, lenient: Boolean = false): JsonObject =
    fromJson(GSON, reader, JsonObject::class.java, lenient)

fun parseArray(content: String?): JsonArray =
    parseArray(StringReader(content))

fun parseArray(reader: Reader?): JsonArray =
    fromJson(GSON, reader, JsonArray::class.java, false)

fun toStableGsonString(json: JsonElement?): String =
    StringWriter().use { stringWriter ->
        JsonWriter(stringWriter).use { jsonWriter ->
            try {
                writeGsonValue(jsonWriter, json, Comparator.naturalOrder<String>())
            } catch (e: IOException) {
                throw AssertionError(e)
            }
        }
        stringWriter.toString()
    }

@Throws(IOException::class)
fun writeGsonValue(writer: JsonWriter, json: JsonElement?, comparator: Comparator<String?>?) {
    when {
        json == null || json.isJsonNull -> writer.nullValue()
        json.isJsonPrimitive -> {
            val jsonPrimitive = json.asJsonPrimitive
            when {
                jsonPrimitive.isNumber -> writer.value(jsonPrimitive.asNumber)
                jsonPrimitive.isBoolean -> writer.value(jsonPrimitive.asBoolean)
                else -> writer.value(jsonPrimitive.asString)
            }
        }

        json.isJsonArray -> {
            writer.beginArray()
            json.asJsonArray.forEach { writeGsonValue(writer, it, comparator) }
            writer.endArray()
        }

        json.isJsonObject -> {
            writer.beginObject()
            sortByKeyIfNeeded(json.asJsonObject.entrySet(), comparator).forEach { (key, value) ->
                writer.name(key)
                writeGsonValue(writer, value, comparator)
            }
            writer.endObject()
        }

        else -> throw IllegalArgumentException("Couldn't write ${json.javaClass}")
    }
}

private fun sortByKeyIfNeeded(
    entries: Set<Map.Entry<String, JsonElement>>,
    comparator: Comparator<String?>?
): Collection<Map.Entry<String, JsonElement>> =
    comparator?.let {
        entries.sortedWith(compareBy(comparator) { it.key })
    } ?: entries

