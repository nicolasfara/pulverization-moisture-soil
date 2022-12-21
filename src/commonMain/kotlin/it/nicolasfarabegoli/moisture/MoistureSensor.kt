package it.nicolasfarabegoli.moisture

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Sensor
import it.nicolasfarabegoli.pulverization.core.SensorsContainer
import org.koin.core.component.inject

expect class MoistureSensor() : Sensor<Double>

class DeviceSensors : SensorsContainer() {
    override val context: Context by inject()

    override suspend fun initialize() {
        this += MoistureSensor()
    }
}
