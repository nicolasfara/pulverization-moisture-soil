package it.nicolasfarabegoli.moisture

import com.pi4j.context.Context
import com.pi4j.ktx.io.analog.analogInput
import com.pi4j.ktx.io.analog.piGpioProvider
import com.pi4j.ktx.pi4j
import it.nicolasfarabegoli.pulverization.core.Sensor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.ServerSocket
import java.util.*
import kotlin.time.Duration.Companion.seconds

@Serializable
data class Moisture(val moisture: Double)

actual class MoistureSensor : Sensor<Double> {
    private lateinit var job: Job
    private lateinit var socketJob: Job
    private lateinit var ctx: Context

    private var moisture: Double = 0.0

    companion object {
        private const val MOISTURE_PIN = 18
    }

    actual suspend fun init() = coroutineScope {
        socketJob = launch(Dispatchers.IO) {
            val server = ServerSocket(1672)
            println("Socket opened")
            val clientSocket = Scanner(server.accept().getInputStream())
            println("rrr")
            while (true) {
                try {
                    val line = clientSocket.nextLine()
                    println("New line: $line")
                    val m = Json.decodeFromString<Moisture>(line)
                    moisture = m.moisture
                } catch (ex: NoSuchElementException) {
                    println(ex)
                    stop()
                }
            }
        }
        job = launch {
            moistureAcquisition()
        }
    }

    actual suspend fun stop() {
        ctx.shutdown()
        job.cancelAndJoin()
    }

    override fun sense(): Double = moisture

    private suspend fun moistureAcquisition() {
        pi4j {
            ctx = this
            val moistureGPIO = analogInput(MOISTURE_PIN) { piGpioProvider() }
            while (true) {
                moisture = moistureGPIO.value.toDouble() // TODO fix it
                delay(2.seconds)
            }
        }
    }
}
