package it.nicolasfarabegoli.moisture

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Actuator
import it.nicolasfarabegoli.pulverization.core.ActuatorsContainer
import org.koin.core.component.inject

expect class ValveActuator() : Actuator<Boolean>

class DeviceActuator : ActuatorsContainer() {
    override val context: Context by inject()

    override suspend fun initialize() {
        this += ValveActuator()
    }
}
