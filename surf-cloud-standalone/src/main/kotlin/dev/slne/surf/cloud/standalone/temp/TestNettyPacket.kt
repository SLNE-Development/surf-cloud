package dev.slne.surf.cloud.standalone.temp

import dev.slne.surf.cloud.api.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.netty.packet.NettyPacket
import dev.slne.surf.cloud.api.netty.protocol.buffer.SurfByteBuf
import java.util.*

@SurfNettyPacket(id = 0xff)
class TestNettyPacket : NettyPacket<TestNettyPacket> {
    private lateinit var test: String
    private var testInt: Int = 0
    private var testBoolean: Boolean = false
    private var testUUID: UUID? = null

    internal constructor()

    constructor(test: String, testInt: Int, testBoolean: Boolean, testUUID: UUID?) {
        this.test = test
        this.testInt = testInt
        this.testBoolean = testBoolean
        this.testUUID = testUUID
    }

    override fun encode(buffer: SurfByteBuf) {
        buffer.writeUtf(test)
        buffer.writeVarInt(testInt)
        buffer.writeBoolean(testBoolean)
        buffer.writeNullable(testUUID)
    }

    override fun decode(buffer: SurfByteBuf): TestNettyPacket {
        test = buffer.readUtf()
        testInt = buffer.readVarInt()
        testBoolean = buffer.readBoolean()
        testUUID = buffer.readNullableUuid()
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TestNettyPacket) return false
        if (!super.equals(other)) return false

        if (test != other.test) return false
        if (testInt != other.testInt) return false
        if (testBoolean != other.testBoolean) return false
        if (testUUID != other.testUUID) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + test.hashCode()
        result = 31 * result + testInt
        result = 31 * result + testBoolean.hashCode()
        result = 31 * result + (testUUID?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "TestNettyPacket(test='$test', testInt=$testInt, testBoolean=$testBoolean, testUUID=$testUUID) ${super.toString()}"
    }


}
