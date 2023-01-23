package it.nicolasfarabegoli.moisture

import it.nicolasfarabegoli.pulverization.core.Sensor

actual class MoistureSensor : Sensor<Double> {
    override suspend fun sense(): Double {
        TODO("Not yet implemented")
    }

    actual suspend fun init() {}
    actual suspend fun stop() {}
}
