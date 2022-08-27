package programmer.zaman.now.coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.selects.select
import org.junit.jupiter.api.Test

class SelectTest {
    /*
    ======== select Function =========

    select Function
    select Function memungkinkan kita untuk menunggu beberapa suspending function dalam sebuah coroutine dan memilih yang pertama datanya tersedia
    select Function bisa digunakan di Deferred dan juga Channel (coroutine yg mengembalikan data)
    Untuk Deffered, kita bisa menggunakan onAwait
    dan untuk ReceiveChannel, kita bisa menggunakan onReceive

    fungsi:
    Select function berfungsi ketika kita ingin mengambil data untuk mengambil data tersedia di awal
     */

    // kode select function untuk deferred
    @Test
    fun testSelectDeferred() {
        val scope = CoroutineScope(Dispatchers.IO)

        val deferred1 = scope.async {
            delay(1000)
            // deferred 1 dengan value 1000 dan delay 1 detik
            1000
        }

        val deferred2 = scope.async {
            delay(2000)
            // deferred 2 dengan value 2000 dan delay 2 detik
            2000
        }

        val deferred3 = scope.async {
            delay(500)
            // deferred 2 dengan value 500 dan delay 0.5 detik
            500
        }

        val job = scope.launch {
            // Use select function
            // Dengan menggunakan select function kita dapat melihat coroutine mana yg datanya paling awal tersedia
            val win = select<String> {
                deferred1.onAwait { "Result $it" }
                deferred2.onAwait { "Result $it" }
                deferred3.onAwait { "Result $it" }
            }
            println("Win : $win")
        }

        runBlocking {
            job.join()
        }
        /*
        output:
        Win : Result 500

        Berdasarkan output terlihat bahwa data yg paling cepat diterima adalah deferred 3 dengan delay 0.5 detik
        Dengan menggunakan select function kita dapat menentukan coroutine mana yg datanya paling cepat diterima
         */
    }

    // kode select function untuk channel
    @Test
    fun testSelectChannel() {
        val scope = CoroutineScope(Dispatchers.IO)

        val receiveChannel1 = scope.produce {
            delay(1000)
            send(1000)
        }

        val receiveChannel2 = scope.produce {
            delay(2000)
            send(2000)
        }

        val receiveChannel3 = scope.produce {
            delay(500)
            send(500)
        }

        val job = scope.launch {
            val win = select<String> {
                receiveChannel1.onReceive { "Result $it" }
                receiveChannel2.onReceive { "Result $it" }
                receiveChannel3.onReceive { "Result $it" }
            }
            println("Win : $win")
        }

        runBlocking {
            job.join()
        }
        /*
        output:
        Win : Result 500

        Hasil output yg ditampilkan oleh select function pada channel sama dengan deferred
        Yakni output nya merupakan data yg pertama kali atau paling cepat diterima oleh receiver
         */
    }

    // kode select function gabungan untuk deferred dan channel
    // kita juga dapat menggabungkan deferred dan channel dalam satu select function
    @Test
    fun testSelectChannelAndDeferred() {
        val scope = CoroutineScope(Dispatchers.IO)

        // menggunakan channel
        val receiveChannel1 = scope.produce {
            delay(100)
            send(100)
        }

        // menggunakan deferred
        val deferred2 = scope.async {
            delay(2000)
            2000
        }

        // menggunakan deferred
        val deferred3 = scope.async {
            delay(500)
            500
        }

        val job = scope.launch {
            val win = select<String> {
                receiveChannel1.onReceive { "Result $it" }
                deferred2.onAwait { "Result $it" }
                deferred3.onAwait { "Result $it" }
            }
            println("Win : $win")
        }

        runBlocking {
            job.join()
        }
        /*
        output:
        Win : Result 100

        Hasil output yg ditampilkan oleh select function gabungan channel dan deferred dengan yg non gabungan sama
        Jadi tidak terdapat perbedaan hasil ketika kita menggunakan select function gabungan ataupun yg non gabungan
         */
    }
}