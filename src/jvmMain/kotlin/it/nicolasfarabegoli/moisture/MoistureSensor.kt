package it.nicolasfarabegoli.moisture

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.utils.io.readUTF8Line
import it.nicolasfarabegoli.pulverization.core.Sensor
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.system.exitProcess

@Serializable
data class Moisture(val moisture: Double)

actual class MoistureSensor : Sensor<Double> {

    private lateinit var listenSensorJob: Job
    private var moisture: Double = 0.0

    @OptIn(DelicateCoroutinesApi::class)
    actual suspend fun init() = coroutineScope {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val sensorDeviceIP = System.getenv("SENSOR_IP") ?: error("Unable to find the sensor IP")
        val sensorDevicePort = System.getenv("SENSOR_PORT").toIntOrNull() ?: error("Unable to find the sensor port")
        val socketClient = aSocket(selectorManager).tcp().connect(sensorDeviceIP, sensorDevicePort)
        val receiveChannel = socketClient.openReadChannel()

        listenSensorJob = GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                receiveChannel.readUTF8Line()?.let {
                    val moisturePayload = Json.decodeFromString<Moisture>(it)
                    moisture = moisturePayload.moisture
                    println("Read moisture: $moisture")
                } ?: run {
                    socketClient.close()
                    selectorManager.close()
                    exitProcess(1)
                }
            }
        }
    }

    actual suspend fun stop() {
        listenSensorJob.cancelAndJoin()
    }

    override suspend fun sense(): Double = moisture
}
