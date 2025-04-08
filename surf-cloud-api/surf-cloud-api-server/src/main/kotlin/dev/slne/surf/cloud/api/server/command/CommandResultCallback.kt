package dev.slne.surf.cloud.api.server.command

fun interface CommandResultCallback {
    fun onResult(success: Boolean, result: Int)
    fun onSuccess(result: Int) = onResult(true, result)
    fun onFailure() = onResult(false, 0)

    operator fun plus(other: CommandResultCallback): CommandResultCallback = chain(this, other)

    companion object {
        val EMPTY = object : CommandResultCallback {
            override fun onResult(success: Boolean, result: Int) = Unit
            override fun toString(): String = "<empty>"
        }

        fun chain(
            first: CommandResultCallback,
            second: CommandResultCallback
        ): CommandResultCallback {
            if (first == EMPTY) return second
            if (second == EMPTY) return first
            return CommandResultCallback { success, result ->
                first.onResult(success, result)
                second.onResult(success, result)
            }
        }
    }
}