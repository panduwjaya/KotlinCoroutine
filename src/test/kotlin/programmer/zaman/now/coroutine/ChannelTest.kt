package programmer.zaman.now.coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import org.junit.jupiter.api.Test

class ChannelTest {
    /*
    ======== Channel ========

    Channel
    Channel adalah fitur di Kotlin Coroutine yang bisa digunakan untuk mentransfer aliran data dari satu tempat ke tempat lain
    Artinya berfungsi untuk mentransfer data dari satu coroutine ke coroutine lainnya
    Channel mirip struktur data queue, dimana ada data masuk dan ada data keluar,biasanya data masuk dari satu buah coroutine dan data keluar nya dari coroutine lainnya
    Untuk mengirim data ke channel, kita bisa menggunakan function send() dan untuk mengambil data di channel kita bisa menggunakan function receive()
    Channel itu sifatnya blocking, artinya jika tidak ada data di channel, saat kita mengambil data menggunakan receive() maka dia akan menunggu sampai ada data. Dan begitu juga ketika ada data di channel, dan tidak ada yang mengambilnya, saat kita send() data, dia akan menunggu sampai channel kosong (datanya diambil)
    Untuk menutup channel, kita bisa menggunakan function close()
    Saat channel sudah di close maka tidak bisa mengirim data maupun menerima data
     */
    @Test
    fun testChannel() {
        runBlocking {

            // membuat channel
            val channel = Channel<Int>()

            // coroutine pertama (coroutine pertama berfungsi untuk mengirim data)
            val job1 = launch {
                println("send data 1 to channel")
                channel.send(1)
                println("send data 2 to channel")
                channel.send(2)
            }

            // coroutine kedua (coroutine kedua berfungsi untuk menerima data)
            val job2 = launch {
                println("receive data ${channel.receive()}")
                println("receive data ${channel.receive()}")
            }

            joinAll(job1, job2)

            // setiap selesai membuat channel jangan lupa untuk men close channel
            channel.close()

        }
        /*
        Output:
        send data 1 to channel
        receive data 1
        send data 2 to channel
        receive data 2

        Catatan:
        Ketika coroutine pengirim meengirim data ke channel maka secara otomatis langsung diterima oleh coroutine penerima
        Saat terdapat coroutine pengirim lalu mengirimkan data ke channel namun tidak terdapat coroutine penerima
        maka secara otomatis channel akan mem-blok semua akses data sampai terdapat coroutine penerima
        Begitupun sebaliknya ketika terdapat coroutine penerima namun tidak ada coroutine pengirim maka semua akses datanya akan diblok
         */
    }
    /*
    ========== Channel Backpressure ==========

    Ketika terjadi peristiwa dimana kemampuan coroutine pengirim (send) lebih cepat dibandingkan dengan coroutine penerima (receive)
    Maka kotlin mempunyai solusi dari permasalahan tersebut,yaitu menggunakan channel pressure
    Dengan menggunakan channel pressure data yang dikirim cepat akan ditampung terlebih dahulu pada channel,kemudian baru diteruskan ke coroutine penerima
    Sehingga kecepatan transfer dari data yang dikirim oleh coroutine pengirim tidak akan melambat mengikuti dari coroutine penerima

    Channel Buffer
    Pengertian Channel buffer adalah kemampuan untuk menampung antrian data dalam sebuah channel sesuai dengan kebutuhan kita
    Secara default, channel hanya bisa menampung satu data, artinya jika kita mencoba mengirim data lain ke channel, maka kita harus menunggu data yang ada diambil.
    Namun kita bisa menambahkan buffer di dalam channel atau istilahnya capacity.Jadi defaultnya capacity nya adalah 0 (buffer atau antrian yang bisa ditampung)
    Fungsi,Jadi selama belum terdapat coroutine penerima namun coroutine pengirim sudah mengirim data maka data tersebut akan ditampung didalam buffer menunggu sampai adanya coroutine penerima

    Contoh Constant Channel Capacity
    format (- Constant = Capacity -> Keterangan)
    Int.MAX_VALUE = bernilai sekitar 2 milliar data yang dapat ditampung dalam antrian channel

    - Channel.UNLIMITED = Int.MAX_VALUE -> Total kapasitas buffer nya Int.MAX_VALUE atau bisa dibilang unlimited
    - Channel.RENDEZVOUS = 0 -> Tidak memiliki buffer
    - Channel.BUFFER = 64 atau bisa di setting via properties -> Total kapasitas buffer nya 64 atau sesuai properties
    - Channel.CONFLATED = -1 -> saat mengirim data
     */

    // TES CHANNEL UNLIMITED
    @Test
    fun testChannelUnlimited() {
        runBlocking {

            // penggunaan channel buffer unlimited
            // artinya dapat menampung data antrian dalam jumlah banyak dari coroutine pengirim sampai terdapat coroutine penerima
            val channel = Channel<Int>(capacity = Channel.UNLIMITED)

            val job1 = launch {
                println("send data 1 to channel")
                channel.send(1)
                println("send data 2 to channel")
                channel.send(2)
            }

            val job2 = launch {
                println("receive data ${channel.receive()}")
                println("receive data ${channel.receive()}")
            }

            joinAll(job1, job2)
            channel.close()

        }
    }

    // TES CHANNEL CONFLATED
    // jadi hanya data terakhir saja yang akan diambil,sedangkan data-data sebelumnya akan dihapus
    @Test
    fun testChannelConflated() {
        runBlocking {

            val channel = Channel<Int>(capacity = Channel.CONFLATED)

            val job1 = launch {
                println("send data 1 to channel")
                channel.send(1)
                println("send data 2 to channel")
                channel.send(2)
            }
            job1.join()

            val job2 = launch {
                println("receive data ${channel.receive()}")
            }
            job2.join()

            channel.close()

        }
        /*
        output:
        send data 1 to channel
        send data 2 to channel
        receive data 2

        Jadi yang diterima hanya data 2 saja,karena yang diambil hanya data terakhir saja sedangkan data-data sebelumnya tidak ikut diambil
         */
    }

    @Test
    fun testChannelBufferOverflow() {
        runBlocking {

            val channel = Channel<Int>(capacity = 10, onBufferOverflow = BufferOverflow.DROP_LATEST)

            val job1 = launch {
                repeat(100) {
                    println("send data $it to channel")
                    channel.send(it)
                }
            }
            job1.join()

            val job2 = launch {
                repeat(10) {
                    println("receive data ${channel.receive()}")
                }
            }
            job2.join()

            channel.close()

        }
    }

    @Test
    fun testUndeliveredElement() {
        val channel = Channel<Int>(capacity = 10) { value ->
            println("Undelivered Element $value")
        }
        channel.close()

        runBlocking {
            val job = launch {
                channel.send(10)
                channel.send(100)
            }
            job.join()
        }
    }

    @Test
    fun testProduce() {
        val scope = CoroutineScope(Dispatchers.IO)
//        val channel = Channel<Int>()
//
//        val job1 = scope.launch {
//            repeat(100) {
//                channel.send(it)
//            }
//        }

        val channel: ReceiveChannel<Int> = scope.produce {
            repeat(100) {
                send(it)
            }
        }

        val job2 = scope.launch {
            repeat(100) {
                println(channel.receive())
            }
        }

        runBlocking {
            joinAll(job2)
        }
    }
}