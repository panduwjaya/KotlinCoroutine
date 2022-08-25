package programmer.zaman.now.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ActorTest {
    /*
    ======== Actor =========

    Actor
    Saat kita menggunakan produce() function, kita membuat coroutine sekaligus sebagai channel sender nya
    Untuk membuat coroutine sekaligus channel receiver, kita bisa menggunakan actor() function
    Konsep seperti dikenal dengan konsep Actor Model
     */
    @Test
    fun testActor() {
        val scope = CoroutineScope(Dispatchers.IO)

        val sendChannel = scope.actor<Int>(capacity = 10) {
            repeat(10) {
                println("Actor receive data ${receive()}")
            }
        }

        val job = scope.launch {
            repeat(10) {
                sendChannel.send(it)
            }
        }

        runBlocking { job.join() }
    }
    /*
    output:
    Actor receive data 0
    Actor receive data 1
    Actor receive data 2
    Actor receive data 3
    Actor receive data 4
    Actor receive data 5
    Actor receive data 6
    Actor receive data 7
    Actor receive data 8
    Actor receive data 9
     */
}