package it.nicolasfarabegoli.moisture.units // ktlint-disable filename

import it.nicolasfarabegoli.moisture.DeviceSensors
import it.nicolasfarabegoli.moisture.configuration
import it.nicolasfarabegoli.moisture.moistureSensorLogic
import it.nicolasfarabegoli.pulverization.dsl.getDeviceConfiguration
import it.nicolasfarabegoli.pulverization.platforms.rabbitmq.RabbitmqCommunicator
import it.nicolasfarabegoli.pulverization.platforms.rabbitmq.defaultRabbitMQRemotePlace
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.sensorsLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.pulverizationPlatform
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val platform = pulverizationPlatform(configuration.getDeviceConfiguration("moisture")!!) {
        sensorsLogic(DeviceSensors(), ::moistureSensorLogic)
        withPlatform { RabbitmqCommunicator() }
        withRemotePlace { defaultRabbitMQRemotePlace() }
    }
    platform.start().joinAll()
    platform.stop()
}
