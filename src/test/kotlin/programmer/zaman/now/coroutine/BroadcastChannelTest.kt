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
    Sehingga apa yang dapat dilakukan oleh broadcast channel dapat dilakukan juga oleh conflated broadcast channel
    Pada Broadcast Channel, walaupun receiver lambat, maka receiver tetap akan mendapatkan seluruh data dari sender
    Namun berbeda dengan Conflated Broadcast Channel, receiver hanya akan mendapat data paling baru dari sender
    Artinya apabila terjadi antrian data dan ternyata terdapat kiriman data terbaru, maka yg diterima dari antrian tsb oleh receiver adalah data yg terbaru
    Jadi jika receiver lambat, receiver hanya akan mendapat data paling baru saja, bukan semua data

    catatan:
    Pada conflacated broadcast channel tidak mempunyai capacity dalam buffer
     */
    @Test
    fun testConflatedBroadcastChannel() {
        val conflatedBroadcastChannel = ConflatedBroadcastChannel<Int>()
        val receiveChannel = conflatedBroadcastChannel.openSubscription()

        val scope = CoroutineScope(Dispatchers.IO)

        // sender dapat mengirim data setiap 1 detik
        val job1 = scope.launch {
            repeat(10){
                delay(1000)
                println("Send $it")
                conflatedBroadcastChannel.send(it)
            }
        }

        // receiver dapat menerima data setiap 2 detik, yg berarti data yg diterima lebih lambat dari data yg dikirim
        // sehingga akan terjadi antrian data, maka data yg diterima oleh receiver hanya data yg tebaru saja didalam antrian tersebut
        val job2 = scope.launch {
            repeat(10){
                delay(2000)
                println("Receive ${receiveChannel.receive()}")
            }
        }

        runBlocking {
            // fungsi dari delay 11 detik adalah agar tidak error pada receive yang tidak seimbang dengan sender dikarenakn jumlah repeat
            delay(11_000)
            scope.cancel()
        }
        /*
        output:
        Send 0
        Receive 0
        Send 1
        Send 2
        Receive 2
        Send 3
        Send 4
        Receive 4
        Send 5
        Send 6
        Receive 6
        Send 7
        Send 8
        Receive 8
        Send 9

        Berdasarkan output diatas terlihat bahwa terdapat beberapa nilai yang tidak diterima oleh receiver
        Hal tersebut terjadi karena sesuai dengan fungsi dari conflacated broadcast channel
        Yaitu jika terjadi antrian pada sebuah data dan terdapat masukan data terbaru dalam antrian maka terbaru itu yg akan diterima oleh receiver
         */
    }
}