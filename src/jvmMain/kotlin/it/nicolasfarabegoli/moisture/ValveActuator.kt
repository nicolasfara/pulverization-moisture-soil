package it.nicolasfarabegoli.moisture

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.writeStringUtf8
import it.nicolasfarabegoli.pulverization.core.Actuator
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

actual class ValveActuator actual constructor() : Actuator<Boolean> {

    private lateinit var listenJob: Job
    private var valveOpened = false

    companion object {
        private const val PORT = 8088
    }

    @OptIn(DelicateCoroutinesApi::class)
    actual suspend fun init() = coroutineScope {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val serverSocket = aSocket(selectorManager).tcp().bind("0.0.0.0", PORT)

        listenJob = GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                val socket = serverSocket.accept()
                println("New connection $socket")
                launch {
                    val sendChannel = socket.openWriteChannel(autoFlush = true)
                    while (true) {
                        sendChannel.writeStringUtf8(if (valveOpened) "1\n" else "0\n")
                        delay(5000.milliseconds)
                    }
                }
            }
        }
    }

    actual suspend fun stop() {
        listenJob.cancelAndJoin()
    }

    override suspend fun actuate(payload: Boolean) {
        valveOpened = payload
    }
}
