package it.nicolasfarabegoli.moisture

import it.nicolasfarabegoli.pulverization.core.Actuator

actual class ValveActuator actual constructor() : Actuator<Boolean> {
    override suspend fun actuate(payload: Boolean) {
        TODO("Not yet implemented")
    }

    actual suspend fun init() {}

    actual suspend fun stop() {}
}
