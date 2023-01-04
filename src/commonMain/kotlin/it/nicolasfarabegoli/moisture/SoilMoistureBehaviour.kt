package it.nicolasfarabegoli.moisture

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Behaviour
import it.nicolasfarabegoli.pulverization.core.BehaviourOutput
import it.nicolasfarabegoli.pulverization.runtime.componentsref.ActuatorsRef
import it.nicolasfarabegoli.pulverization.runtime.componentsref.CommunicationRef
import it.nicolasfarabegoli.pulverization.runtime.componentsref.SensorsRef
import it.nicolasfarabegoli.pulverization.runtime.componentsref.StateRef
import it.nicolasfarabegoli.pulverization.runtime.dsl.NoComm
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import org.koin.core.component.inject

class SoilMoistureBehaviour : Behaviour<StateOps, NoComm, Double, Boolean, Unit> {
    override val context: Context by inject()

    companion object {
        private const val TARGET_MOISTURE = 30.0
    }

    override fun invoke(
        state: StateOps,
        export: List<NoComm>,
        sensedValues: Double,
    ): BehaviourOutput<StateOps, NoComm, Boolean, Unit> {
        val action = sensedValues < TARGET_MOISTURE
        return BehaviourOutput(MoistureState(sensedValues), NoComm, action, Unit)
    }
}

@Suppress("DestructuringDeclarationWithTooManyEntries")
suspend fun moistureBehaviourLogic(
    behaviour: Behaviour<StateOps, NoComm, Double, Boolean, Unit>,
    stateRef: StateRef<StateOps>,
    commRef: CommunicationRef<NoComm>,
    sensorsRef: SensorsRef<Double>,
    actuatorsRef: ActuatorsRef<Boolean>,
) = coroutineScope {
    sensorsRef.receiveFromComponent().collect {
        stateRef.sendToComponent(GetState)
        val currentState = stateRef.receiveFromComponent().first()
        val (newState, _, actuation, _) = behaviour(currentState, listOf(NoComm), it)
        stateRef.sendToComponent(newState)
        actuatorsRef.sendToComponent(actuation)
    }
}
