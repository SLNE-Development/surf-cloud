package dev.slne.surf.cloud.core.client.netty.state

import dev.slne.surf.surfapi.core.api.util.object2ObjectMapOf
import java.util.*

class StateMachine {

    @Volatile
    var state: State = State.DISCONNECTED
        private set


    fun setState(state: State): Boolean {
        if (this.state == state) return true
        if (!State.isValidTransition(this.state, state)) return false

        this.state = state
        return true
    }

    enum class State {
        CONNECTING,
        CONNECTED,
        DEGRADED,
        DISCONNECTED;

        companion object {
            private val transitions = object2ObjectMapOf<State, EnumSet<State>>(
                DISCONNECTED to EnumSet.of(CONNECTING),
                CONNECTING to EnumSet.of(CONNECTED, DEGRADED, DISCONNECTED),
                CONNECTED to EnumSet.of(DEGRADED, DISCONNECTED),
                DEGRADED to EnumSet.of(CONNECTING, DISCONNECTED)
            )

            fun isValidTransition(from: State, to: State): Boolean {
                return transitions[from]?.contains(to) ?: false
            }
        }
    }
}