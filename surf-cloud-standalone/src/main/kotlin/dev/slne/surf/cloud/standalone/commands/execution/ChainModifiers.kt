package dev.slne.surf.cloud.standalone.commands.execution

import kotlin.experimental.and
import kotlin.experimental.or

@JvmInline
value class ChainModifiers(val flags: Byte) {

    private fun setFlag(flag: Byte): ChainModifiers =
        ChainModifiers(flags or flag).takeIf { it.flags != this.flags } ?: this

    fun isForked(): Boolean = flags and FLAG_FORKED != 0.toByte()
    fun setForked(): ChainModifiers = setFlag(FLAG_FORKED)

    fun isReturn(): Boolean = flags and FLAG_IS_RETURN != 0.toByte()
    fun setReturn(): ChainModifiers = setFlag(FLAG_IS_RETURN)

    companion object {
        val DEFAULT = ChainModifiers(0)
        private const val FLAG_FORKED: Byte = 1
        private const val FLAG_IS_RETURN: Byte = 2
    }
}