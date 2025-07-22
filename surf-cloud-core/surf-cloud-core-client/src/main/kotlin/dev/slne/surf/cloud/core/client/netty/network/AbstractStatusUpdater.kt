package dev.slne.surf.cloud.core.client.netty.network

import java.util.concurrent.atomic.AtomicReference

typealias StatusUpdate = (String) -> Unit

abstract class AbstractStatusUpdater(initialState: State, val updateStatus: StatusUpdate) {
    private val state = AtomicReference(initialState)

    fun switchState(newState: State) {
        val updatedState = this.state.updateAndGet {
            check(newState.fromStates.contains(it)) { "Tried to switch to $newState from $it, but expected one of ${newState.fromStates}" }
            newState
        }
        this.updateStatus(updatedState.stateIndication)
    }

    fun getState(): State {
        return state.get()
    }

    enum class State(val stateIndication: String, val fromStates: Set<State> = setOf()) {
        CONNECTING("Connecting to the server..."),
        AUTHORIZING("Logging in...", setOf(CONNECTING)),
        ENCRYPTING("Encrypting...", setOf(AUTHORIZING)),
        PREPARE_CONNECTION("Preparing connection...", setOf(ENCRYPTING, CONNECTING)),
        PRE_PRE_RUNNING("Running initial setup...", setOf(PREPARE_CONNECTION)),
        PRE_RUNNING("Waiting till client started...", setOf(PRE_PRE_RUNNING)),
        SYNCHRONIZING("Synchronizing...", setOf(PRE_RUNNING)),
        SYNCHRONIZE_WAIT_FOR_SERVER("Waiting for server to finish synchronization...", setOf(SYNCHRONIZING)),
        SYNCHRONIZED("Finished synchronization!", setOf(SYNCHRONIZING, SYNCHRONIZE_WAIT_FOR_SERVER)),
        CONNECTED("Connected!", setOf(SYNCHRONIZED))
    }
}

class StatusUpdaterImpl(initialState: State, updateStatus: StatusUpdate) : AbstractStatusUpdater(initialState, updateStatus)