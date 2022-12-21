package it.nicolasfarabegoli.moisture

import it.nicolasfarabegoli.pulverization.component.Context
import it.nicolasfarabegoli.pulverization.core.Sensor
import it.nicolasfarabegoli.pulverization.core.SensorsContainer
import it.nicolasfarabegoli.pulverization.runtime.componentsref.BehaviourRef
import kotlinx.coroutines.delay
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.milliseconds

expect class MoistureSensor() : Sensor<Double>

class DeviceSensors : SensorsContainer() {
    override val context: Context by inject()

    override suspend fun initialize() {
        this += MoistureSensor()
    }
}

suspend fun actuatorLogic(sensors: SensorsContainer, behaviour: BehaviourRef<Double>) {
    sensors.get<MoistureSensor> {
        while (true) {
            behaviour.sendToComponent(sense())
            delay(500.milliseconds)
        }
    }
}
