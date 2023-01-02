package it.nicolasfarabegoli.moisture

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.State
import it.nicolasfarabegoli.pulverization.core.StateRepresentation
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.serialization.Serializable
import org.koin.core.component.inject

@Serializable
sealed interface StateOps : StateRepresentation

@Serializable
object GetState : StateOps

@Serializable
data class MoistureState(val moisture: Double) : StateOps

class DeviceState : State<StateOps> {
    override val context: Context by inject()
    private var state = MoistureState(0.0)

    override fun get(): StateOps = state

    override fun update(newState: StateOps): StateOps {
        return when (newState) {
            is GetState -> state
            is MoistureState -> {
                val old = state
                state = MoistureState(newState.moisture)
                old
            }
        }
    }
}

suspend fun moistureStateLogic(state: State<StateOps>, behaviour: BehaviourRef<StateOps>) {
    behaviour.receiveFromComponent().collect {
        when (it) {
            is GetState -> behaviour.sendToComponent(state.get())
            is MoistureState -> state.update(it)
        }
    }
}
