package it.nicolasfarabegoli.moisture.units

import it.nicolasfarabegoli.moisture.DeviceState
import it.nicolasfarabegoli.moisture.SoilMoistureBehaviour
import it.nicolasfarabegoli.moisture.configuration
import it.nicolasfarabegoli.moisture.moistureBehaviourLogic
import it.nicolasfarabegoli.moisture.moistureStateLogic
import it.nicolasfarabegoli.pulverization.dsl.getDeviceConfiguration
import it.nicolasfarabegoli.pulverization.platforms.rabbitmq.RabbitmqCommunicator
import it.nicolasfarabegoli.pulverization.platforms.rabbitmq.defaultRabbitMQRemotePlace
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.behaviourLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.PulverizationPlatformScope.Companion.stateLogic
import it.nicolasfarabegoli.pulverization.runtime.dsl.pulverizationPlatform
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val platform = pulverizationPlatform(configuration.getDeviceConfiguration("moisture")!!) {
        withRemotePlace { defaultRabbitMQRemotePlace() }
        withPlatform { RabbitmqCommunicator(hostname = "rabbitmq") }
        behaviourLogic(SoilMoistureBehaviour(), ::moistureBehaviourLogic)
        stateLogic(DeviceState(), ::moistureStateLogic)
    }
    platform.start().joinAll()
    platform.stop()
}
