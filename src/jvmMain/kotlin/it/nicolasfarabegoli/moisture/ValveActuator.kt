package it.nicolasfarabegoli.moisture

import com.pi4j.context.Context
import com.pi4j.io.gpio.digital.DigitalState
import com.pi4j.ktx.io.digital.digitalOutput
import com.pi4j.ktx.pi4j
import it.nicolasfarabegoli.pulverization.core.Actuator
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

actual class ValveActuator actual constructor() : Actuator<Boolean> {
    private lateinit var job: Job
    private lateinit var ctx: Context
    private val channel = Channel<Boolean>()

    companion object {
        private const val VALVE_PIN = 22
    }

    actual suspend fun init() = coroutineScope {
        job = launch { valveManagement() }
    }

    actual suspend fun stop() {
        ctx.shutdown()
        job.cancelAndJoin()
    }

    override fun actuate(payload: Boolean) {}

    private suspend fun valveManagement() {
        pi4j {
            ctx = this
            val valveGPIO = digitalOutput(VALVE_PIN) { initial(DigitalState.LOW) }
            while (true) {
                valveGPIO.setState(channel.receive())
            }
        }
    }
}
