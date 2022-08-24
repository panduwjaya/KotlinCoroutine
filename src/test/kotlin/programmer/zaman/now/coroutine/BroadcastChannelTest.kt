package programmer.zaman.now.coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.broadcast
import org.junit.jupiter.api.Test

class BroadcastChannelTest {
    /*
    ===== Broadcast Channel ======

    Broadcast Channel
    Secara default channel hanya boleh memiliki 1 receiver
    Apabila kita paksa untuk memiliki 2 receiver dalam dalam channel yang sama,maka yg dapat menerima data hanya 1 receiver saja
    Namun Kotlin Coroutine mendukung Broadcast Channel, ini adalah channel khusus yang receiver nya bisa lebih dari satu
    Setiap kita mengirim data ke channel ini, secara otomatis semua receiver bisa mendapatkan data tersebut
    BroadcastChannel memiliki function openSubscription() untuk membuat ReceiveChannel baru
    Broadcast channel tidak mendukung kapasitas buffer 0 dan UNLIMITED,jadi pastikan masukan buffer yang bukan 0 dan unlimited
     */

    // kode Broadcast channel
    @Test
    fun testBroadcastChannel() {
        val broadcastChannel = BroadcastChannel<Int>(capacity = 10)

        val receiveChannel1 = broadcastChannel.openSubscription()
        val receiveChannel2 = broadcastChannel.openSubscription()

        val scope = CoroutineScope(Dispatchers.IO)

        val jobSend = scope.launch {
            repeat(3){
                broadcastChannel.send(it)
            }
        }

        val job1 = scope.launch {
            repeat(3){
                println("Job 1 dengan data = ${receiveChannel1.receive()}")
            }
        }

        val job2 = scope.launch {
            repeat(3){
                println("Job 2 dengan data = ${receiveChannel2.receive()}")
            }
        }

        runBlocking {
            joinAll(job1, job2, jobSend)
        }
        /*
        output:
        Job 1 dengan data = 0
        Job 1 dengan data = 1
        Job 1 dengan data = 2
        Job 2 dengan data = 0
        Job 2 dengan data = 1
        Job 2 dengan data = 2

        Berdasarkan output dapat kita tentukan bahwa sebuah sebuah channel dapat mengirim data yg sama ke beberapa receiver berbeda menggunakan channel broadcast
         */
    }

    /*
    broadcast Function

    Sama seperti produce function, untuk membuat broadcast channel secara langsung dengan coroutine nya,
    kita bisa menggunakan function broadcast di coroutine scope
    Hasil dari broadcast function adalah BroadcastChannel
     */

    // kode Broadcast Function
    @Test
    fun testBroadcastFunction() {
        val scope = CoroutineScope(Dispatchers.IO)

//        val broadcastChannel = BroadcastChannel<Int>(capacity = 10)
//        val jobSend = scope.launch {
//            repeat(10){
//                broadcastChannel.send(it)
//            }
//        }

        val broadcastChannel = scope.broadcast<Int>(capacity = 10) {
            repeat(3){
                send(it)
            }
        }

        val receiveChannel1 = broadcastChannel.openSubscription()
        val receiveChannel2 = broadcastChannel.openSubscription()

        val job1 = scope.launch {
            repeat(3){
                println("Job 1 ${receiveChannel1.receive()}")
            }
        }

        val job2 = scope.launch {
            repeat(3){
                println("Job 2 ${receiveChannel2.receive()}")
            }
        }

        runBlocking {
            joinAll(job1, job2)
        }
        /*
        Job 1 dengan data = 0
        Job 1 dengan data = 1
        Job 1 dengan data = 2
        Job 2 dengan data = 0
        Job 2 dengan data = 1
        Job 2 dengan data = 2

        Hasil output yang ditampilkan oleh broadcast funtion sama dengan yg menggunakan function broadcast Channel
         */
    }

    /*
    ======== Conflated Broadcast Channel =========

    Conflated Broadcast Channel

    Conflated Broadcast Channel adalah turunan dari Broadcast Channel, sehingga cara kerjanya sama
    Pada Broadcast Channel, walaupun receiver lambat, maka receiver tetap akan mendapatkan seluruh data dari sender
    Namun berbeda dengan Conflated Broadcast Channel, receiver hanya akan mendapat data paling baru dari sender
    Jadi jika receiver lambat, receiver hanya akan mendapat data paling baru saja, bukan semua data
     */
    @Test
    fun testConflatedBroadcastChannel() {
        val conflatedBroadcastChannel = ConflatedBroadcastChannel<Int>()
        val receiveChannel = conflatedBroadcastChannel.openSubscription()

        val scope = CoroutineScope(Dispatchers.IO)

        val job1 = scope.launch {
            repeat(10){
                delay(1000)
                println("Send $it")
                conflatedBroadcastChannel.send(it)
            }
        }

        val job2 = scope.launch {
            repeat(10){
                delay(2000)
                println("Receive ${receiveChannel.receive()}")
            }
        }

        runBlocking {
            delay(11_000)
            scope.cancel()
        }
    }
}