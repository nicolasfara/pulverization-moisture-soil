package it.nicolasfarabegoli.moisture

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Behaviour
import it.nicolasfarabegoli.pulverization.core.BehaviourOutput
import it.nicolasfarabegoli.pulverization.runtime.dsl.NoComm
import org.koin.core.component.inject

class SoilMoistureBehaviour : Behaviour<StateOps, NoComm, Double, Boolean, Unit> {
    override val context: Context by inject()

    companion object {
        private const val TARGET_MOISTURE = 0.75
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
