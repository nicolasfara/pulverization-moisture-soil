package it.nicolasfarabegoli.moisture.units

import it.nicolasfarabegoli.moisture.DeviceActuator
import it.nicolasfarabegoli.moisture.configuration
import it.nicolasfarabegoli.moisture.valveActuatorsLogic
import it.nicolasfarabegoli.pulverization.dsl.getDeviceConfiguration
import it.nicolasfarabegoli.pulverization.platforms.rabbitmq.RabbitmqCommunicator
import it.nicolasfarabegoli.pulverization.platforms.rabbitmq.defaultRabbitMQRemotePlace
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.actuatorsLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.pulverizationPlatform
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val platform = pulverizationPlatform(configuration.getDeviceConfiguration("moisture")!!) {
        withRemotePlace { defaultRabbitMQRemotePlace() }
        withPlatform { RabbitmqCommunicator(hostname = "rabbitmq") }
        actuatorsLogic(DeviceActuator(), ::valveActuatorsLogic)
    }
    platform.start().joinAll()
    platform.stop()
}
