package dev.slne.surf.cloud.standalone.spring.config.redis

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.*
import java.math.BigDecimal
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.UnaryOperator
import java.util.stream.Stream
import java.util.stream.StreamSupport

class FasterXmlJsonOps(
    private val compressed: Boolean,
    private val nodeFactory: JsonNodeFactory = JsonNodeFactory.instance
) : DynamicOps<JsonNode> {

    companion object {
        val INSTANCE = FasterXmlJsonOps(false)
        val COMPRESSED = FasterXmlJsonOps(true)
    }

    override fun empty(): JsonNode = NullNode.getInstance()

    override fun <U> convertTo(outOps: DynamicOps<U>, input: JsonNode): U {
        return when {
            input.isObject -> convertMap(outOps, input)
            input.isArray -> convertList(outOps, input)
            input.isNull -> outOps.empty()
            input.isTextual -> outOps.createString(input.asText())
            input.isBoolean -> outOps.createBoolean(input.booleanValue())
            input.isNumber -> {
                val bd = input.decimalValue()
                try {
                    val l = bd.longValueExact()
                    when (l) {
                        l.toByte().toLong() -> outOps.createByte(l.toByte())
                        l.toShort().toLong() -> outOps.createShort(l.toShort())
                        l.toInt().toLong() -> outOps.createInt(l.toInt())
                        else -> outOps.createLong(l)
                    }
                } catch (_: ArithmeticException) {
                    val d = bd.toDouble()
                    if (d.toFloat().toDouble() == d) outOps.createFloat(d.toFloat())
                    else outOps.createDouble(d)
                }
            }

            else -> outOps.empty()
        }
    }

    override fun getNumberValue(input: JsonNode): DataResult<Number> {
        if (input.isNumber) {
            return DataResult.success(input.numberValue())
        }
        if (compressed && input.isTextual) {
            return try {
                DataResult.success(input.asText().toInt())
            } catch (e: NumberFormatException) {
                DataResult.error { "Not a number: $e $input" }
            }
        }
        return DataResult.error { "Not a number: $input" }
    }

    override fun createNumeric(i: Number): JsonNode = when (i) {
        is Byte -> IntNode.valueOf(i.toInt())
        is Short -> ShortNode.valueOf(i)
        is Int -> IntNode.valueOf(i)
        is Long -> LongNode.valueOf(i)
        is Float -> FloatNode.valueOf(i)
        is Double -> DoubleNode.valueOf(i)
        is BigDecimal -> DecimalNode(i)
        else -> DecimalNode(BigDecimal(i.toString()))
    }

    override fun getBooleanValue(input: JsonNode): DataResult<Boolean> {
        return if (input.isBoolean) DataResult.success(input.booleanValue())
        else DataResult.error { "Not a boolean: $input" }
    }

    override fun createBoolean(value: Boolean): JsonNode = BooleanNode.valueOf(value)

    override fun getStringValue(input: JsonNode): DataResult<String> {
        if (input.isTextual || (compressed && input.isNumber)) {
            return DataResult.success(input.asText())
        }
        return DataResult.error { "Not a string: $input" }
    }

    override fun createString(value: String): JsonNode = TextNode.valueOf(value)

    override fun mergeToList(list: JsonNode, value: JsonNode): DataResult<JsonNode> {
        if (!list.isArray && !list.isNull) {
            return DataResult.error({ "mergeToList called with not a list: $list" }, list)
        }
        val arr = ArrayNode(nodeFactory)
        if (!list.isNull) arr.addAll(list as ArrayNode)
        arr.add(value)
        return DataResult.success(arr)
    }

    override fun mergeToList(list: JsonNode, values: List<JsonNode>): DataResult<JsonNode> {
        if (!list.isArray && !list.isNull) {
            return DataResult.error({ "mergeToList called with not a list: $list" }, list)
        }
        val arr = ArrayNode(nodeFactory)
        if (!list.isNull) arr.addAll(list as ArrayNode)
        values.forEach(arr::add)
        return DataResult.success(arr)
    }

    override fun mergeToMap(map: JsonNode, key: JsonNode, value: JsonNode): DataResult<JsonNode> {
        if (!map.isObject && !map.isNull) {
            return DataResult.error({ "mergeToMap called with not a map: $map" }, map)
        }
        if (!key.isTextual && !compressed) {
            return DataResult.error({ "key is not a string: $key" }, map)
        }
        val obj = ObjectNode(nodeFactory)
        if (!map.isNull) obj.setAll<JsonNode>(map as ObjectNode)
        obj.set<JsonNode>(key.asText(), value)
        return DataResult.success(obj)
    }

    override fun mergeToMap(map: JsonNode, values: MapLike<JsonNode>): DataResult<JsonNode> {
        if (!map.isObject && !map.isNull) {
            return DataResult.error({ "mergeToMap called with not a map: $map" }, map)
        }
        val obj = ObjectNode(nodeFactory)
        if (!map.isNull) obj.setAll<JsonNode>(map as ObjectNode)

        val missed = mutableListOf<JsonNode>()
        values.entries().forEach { pair ->
            val k = pair.first
            if (!k.isTextual && !compressed) {
                missed.add(k)
            } else {
                obj.set<JsonNode>(k.asText(), pair.second)
            }
        }

        if (missed.isNotEmpty()) {
            return DataResult.error({ "some keys are not strings: $missed" }, obj)
        }
        return DataResult.success(obj)
    }

    override fun getMapValues(input: JsonNode): DataResult<Stream<Pair<JsonNode, JsonNode>>> {
        if (!input.isObject) return DataResult.error { "Not a JSON object: $input" }
        val stream = input.properties().stream()
            .map { e ->
                Pair.of<JsonNode, JsonNode>(
                    TextNode.valueOf(e.key),
                    if (e.value.isNull) null else e.value
                )
            }

        return DataResult.success(stream)
    }

    override fun getMapEntries(input: JsonNode): DataResult<Consumer<BiConsumer<JsonNode, JsonNode?>>> {
        if (!input.isObject) return DataResult.error { "Not a JSON object: $input" }
        return DataResult.success(Consumer { c ->
            input.properties().forEach { (k, v) ->
                c.accept(createString(k), if (v.isNull) null else v)
            }
        })
    }

    override fun getMap(input: JsonNode): DataResult<MapLike<JsonNode>> {
        if (!input.isObject) return DataResult.error { "Not a JSON object: $input" }
        val obj = input as ObjectNode
        return DataResult.success(object : MapLike<JsonNode> {
            override fun get(key: JsonNode): JsonNode? {
                val v = obj.get(key.asText())
                return if (v?.isNull == true) null else v
            }

            override fun get(key: String): JsonNode? {
                val v = obj.get(key)
                return if (v?.isNull == true) null else v
            }

            override fun entries(): Stream<Pair<JsonNode, JsonNode>> =
                obj.properties().stream()
                    .map { e -> Pair.of(TextNode.valueOf(e.key), e.value) }

            override fun toString(): String = "MapLike[$obj]"
        })
    }

    override fun createMap(map: Stream<Pair<JsonNode, JsonNode>>): JsonNode {
        val obj = ObjectNode(nodeFactory)
        map.forEach { p -> obj.set<JsonNode>(p.first.asText(), p.second) }
        return obj
    }

    override fun getStream(input: JsonNode): DataResult<Stream<JsonNode?>> {
        if (!input.isArray) return DataResult.error { "Not a json array: $input" }
        val s = StreamSupport.stream((input as ArrayNode).spliterator(), false)
            .map { e -> if (e.isNull) null else e }

        return DataResult.success(s)
    }

    override fun getList(input: JsonNode): DataResult<Consumer<Consumer<JsonNode?>>> {
        if (!input.isArray) return DataResult.error { "Not a json array: $input" }
        val arr = input as ArrayNode
        return DataResult.success(Consumer { consumer ->
            arr.forEach { el -> consumer.accept(if (el.isNull) null else el) }
        })
    }

    override fun createList(input: Stream<JsonNode>): JsonNode {
        val arr = ArrayNode(nodeFactory)
        input.forEach(arr::add)
        return arr
    }


    override fun remove(input: JsonNode, key: String): JsonNode {
        if (input.isObject) {
            val out = ObjectNode(nodeFactory)
            input.properties().forEach { (k, v) ->
                if (k != key) out.set<JsonNode>(k, v)
            }
            return out
        }
        return input
    }

    override fun toString(): String = "FasterXmlJsonOps(compressed=$compressed)"

    override fun listBuilder(): ListBuilder<JsonNode> = ArrayBuilder()

    private inner class ArrayBuilder : ListBuilder<JsonNode> {
        private var builder: DataResult<ArrayNode> =
            DataResult.success(ArrayNode(nodeFactory), Lifecycle.stable())

        override fun ops(): DynamicOps<JsonNode> = this@FasterXmlJsonOps

        override fun add(value: JsonNode): ListBuilder<JsonNode> {
            builder = builder.map { b -> b.add(value); b }
            return this
        }

        override fun add(value: DataResult<JsonNode>): ListBuilder<JsonNode> {
            builder = builder.apply2stable({ b, e -> b.add(e); b }, value)
            return this
        }

        override fun withErrorsFrom(result: DataResult<*>): ListBuilder<JsonNode> {
            builder = builder.flatMap { r -> result.map { r } }
            return this
        }

        override fun mapError(onError: UnaryOperator<String>): ListBuilder<JsonNode> {
            builder = builder.mapError(onError)
            return this
        }

        override fun build(prefix: JsonNode): DataResult<JsonNode> {
            val res = builder.flatMap { b ->
                if (!prefix.isArray && !prefix.isNull) {
                    return@flatMap DataResult.error(
                        { "Cannot append a list to not a list: $prefix" },
                        prefix
                    )
                }
                val arr = ArrayNode(nodeFactory)
                if (!prefix.isNull) arr.addAll(prefix as ArrayNode)
                arr.addAll(b)
                DataResult.success(arr, Lifecycle.stable())
            }
            builder = DataResult.success(ArrayNode(nodeFactory), Lifecycle.stable())
            return res
        }
    }

    override fun compressMaps(): Boolean = compressed
    override fun mapBuilder(): RecordBuilder<JsonNode> = JsonRecordBuilder()

    private inner class JsonRecordBuilder :
        RecordBuilder.AbstractStringBuilder<JsonNode, ObjectNode>(this@FasterXmlJsonOps) {

        override fun initBuilder(): ObjectNode = ObjectNode(nodeFactory)

        override fun append(key: String, value: JsonNode, builder: ObjectNode): ObjectNode {
            builder.set<JsonNode>(key, value)
            return builder
        }

        override fun build(builder: ObjectNode, prefix: JsonNode?): DataResult<JsonNode> {
            if (prefix == null || prefix.isNull) {
                return DataResult.success(builder)
            }
            if (prefix.isObject) {
                val result = ObjectNode(nodeFactory)
                (prefix as ObjectNode).properties()
                    .forEach { (k, v) -> result.set<JsonNode>(k, v) }
                builder.properties().forEach { (k, v) -> result.set<JsonNode>(k, v) }
                return DataResult.success(result)
            }
            return DataResult.error({ "mergeToMap called with not a map: $prefix" }, prefix)
        }
    }
}