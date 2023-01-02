package it.nicolasfarabegoli.moisture

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Actuator
import it.nicolasfarabegoli.pulverization.core.ActuatorsContainer
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import org.koin.core.component.inject

expect class ValveActuator() : Actuator<Boolean> {
    suspend fun init()
    suspend fun stop()
}

class DeviceActuator : ActuatorsContainer() {
    override val context: Context by inject()

    override suspend fun initialize() {
        val valveActuator = ValveActuator().apply { init() }
        this += valveActuator
    }

    override suspend fun finalize() {
        get<ValveActuator> { stop() }
    }
}

suspend fun valveActuatorsLogic(actuators: ActuatorsContainer, behaviour: BehaviourRef<Boolean>) {
    actuators.get<ValveActuator> {
        behaviour.receiveFromComponent().collect {
            actuate(it)
        }
    }
}
