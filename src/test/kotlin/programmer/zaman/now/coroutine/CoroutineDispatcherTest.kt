package programmer.zaman.now.coroutine

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors

class CoroutineDispatcherTest {

    /*
    ================ Coroutine Dispatcher =================

    Coroutine Dispatcher
    Selain ada Job di dalam CoroutineContext, ada juga object CoroutineDispatcher.jadi CoroutineDispatcher termasuk turunan CoroutineContext
    CoroutineDispatcher digunakan untuk menentukan thread mana yang bertanggung jawab untuk mengeksekusi coroutine
    Secara default sudah ada setting default dispatcher, namun kita bisa menggantinya jika kita mau artinya kita bisa melakukan custom dispatcher

    Dispatchers
    Ada object Dispatchers yang bisa kita gunakan untuk mengganti CoroutineDispatcher
    Dispatchers.Default = ini adalah default dispatcher, isinya minimal 2 thread, atau sebanyak jumlah cpu (mana yang lebih banyak). Dispatcher ini cocok untuk proses coroutine yang cpu-bound
    Dispatcher.IO = ini adalah dispatcher yang berisikan thread sesuai dengan kebutuhan, ketika butuh akan dibuat, ketika sudah tidak dibutuhkan, akan dihapus, mirip cache thread pool di executor service. Dispatcher ini akan sharing thread dengan Default dispatcher
    Dispatchers.Main = ini adalah dispatchers yang berisikan main thread UI, cocok ketika kita butuh running di thread main seperti di Java Swing, JavaFX atau Android. Untuk menggunakan ini, kita harus menambah library ui tambahan
    jika kita ingin menggunakan Dispatchers.Main pada android kita harus menggunakan library coroutine android
     */

    @Test
    fun testDispatcher() {
        runBlocking {
            println("runBlocking ${Thread.currentThread().name}") // runBlocking Test worker @coroutine#1
            // kita bisa memasukan Dispatchers.Default dan Dispatchers.IO kedalam parameter launch karena CoroutineDispatcher merupakan turunan CoroutineContext
            val job1 = GlobalScope.launch(Dispatchers.Default) {
                println("Job 1 ${Thread.currentThread().name}") // Job 1 DefaultDispatcher-worker-1 @coroutine#2
            }
            val job2 = GlobalScope.launch(Dispatchers.IO) {
                println("Job 2 ${Thread.currentThread().name}") // Job 2 DefaultDispatcher-worker-3 @coroutine#3
            }
            joinAll(job1, job2)
        }
    }

    /*
    Unconfined vs Confined
    Selain Default, IO dan Main, ada juga beberapa dispatchers yang lain
    * Dispatchers.Unconfined, ini adalah dispatcher yang tidak menunjuk thread apapun, biasanya akan melanjutkan thread di coroutine sebelumnya
    * Confined (tanpa parameter), ini adalah dispatcher yang akan melanjutkan thread dari coroutine sebelumnya jadi ketika kita membuat dispatcher tanpa parameter maka secara tidak langsung akan menjadi DispatcherConfined

    Apa bedanya Unconfined dan Confined, pada Unconfined, thread bisa berubah di tengah jalan jika memang terdapat code yang melakukan perubahan thread
    * ketika menggunakan confined saat kita menggunakan thread A,maka sampai selesai akan menggunakan Thread A saja
    * sedangkan Unconfined saat kita menggunakan thread A dan di tengah jalan terdapat code yang melakukan perubahan Thread,bisa saja menyelesaikan proses menggunakan Thread B atau yg lain bukan Thread A
      artinya ketika kita menggunakan Unconfined kita bisa secara fleksibel berpindah dari satu Thread ke Thread lain nya apabila terdapat code yg melakukan perubahan Thread di tengah jalan

     */
    @Test
    fun testUnconfined() {
        runBlocking {
            println("runBlocking ${Thread.currentThread().name}") // runBlocking Test worker @coroutine#1

            GlobalScope.launch(Dispatchers.Unconfined) {
                println("Unconfined : ${Thread.currentThread().name}") // Unconfined : Test worker @coroutine#2
                delay(1000) // fungsi dari delay adalah untuk menjeda eksekusi dan memindahkan thread yg digunakan ke eksekusi lain
                println("Unconfined : ${Thread.currentThread().name}") // Unconfined : kotlinx.coroutines.DefaultExecutor @coroutine#2
                delay(1000)
                println("Unconfined : ${Thread.currentThread().name}") // Unconfined : kotlinx.coroutines.DefaultExecutor @coroutine#2
            }
            GlobalScope.launch {
                println("Confined : ${Thread.currentThread().name}") // Confined : DefaultDispatcher-worker-1 @coroutine#3
                delay(1000)
                println("Confined : ${Thread.currentThread().name}") // Confined : DefaultDispatcher-worker-1 @coroutine#3
                delay(1000)
                println("Confined : ${Thread.currentThread().name}") // Confined : DefaultDispatcher-worker-1 @coroutine#3
            }

            delay(3000)

            /*
            output:
            runBlocking Test worker @coroutine#1
            Unconfined : Test worker @coroutine#2 -> eksekusi pertama unconfined
            Confined : DefaultDispatcher-worker-1 @coroutine#3 -> eksekusi pertama confined
            Unconfined : kotlinx.coroutines.DefaultExecutor @coroutine#2 -> eksekusi kedua unconfined
            Confined : DefaultDispatcher-worker-1 @coroutine#3 -> eksekusi kedua confined
            Unconfined : kotlinx.coroutines.DefaultExecutor @coroutine#2 -> eksekusi ketiga unconfined
            Confined : DefaultDispatcher-worker-1 @coroutine#3 -> eksekusi ketiga confined

            notes:
            1) Thread yang digunakan pada eksekusi pertama dan kedua pada unconfined akan berbeda karena terdapat perpindahan Thread
            Pada unconfined akan terjadi perpindahan thread artinya fleksibel apabila terdapat code yg melakukan perubahan Thread seperti delay dll
            Namun bisa terjadi Thread yg digunakan tidak berubah,hal itu bisa terjadi dalam keadaan tertntu seperti pada esksekusi kedua dan ketiga unconfined yg menggunakan Thread yg sama

            2) Thread yg digunakan pada eksekusi pertama,kedua dan ketiga pada confined selalu sama dikarenakan pada confined Thread yg digunakan dari awal sampai akhir adalah sama
            berbeda dengan unconfined yg bersifat fleksibel yg dapat berpindah thread apabila terdapat kode yg melakukan perpindahan Thread seperti delay() dll
             */
        }
    }

    @Test
    fun testExecutorService() {
        val dispatcherService = Executors.newFixedThreadPool(10).asCoroutineDispatcher()
        val dispatcherWeb = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

        runBlocking {
            val job1 = GlobalScope.launch(dispatcherService) {
                println("Job 1 : ${Thread.currentThread().name}")
            }
            val job2 = GlobalScope.launch(dispatcherWeb) {
                println("Job 2 : ${Thread.currentThread().name}")
            }
            joinAll(job1, job2)
        }
    }

    @Test
    fun testWithContext() {
        val dispatcherClient = Executors.newFixedThreadPool(10).asCoroutineDispatcher()
        runBlocking {
            val job = GlobalScope.launch(Dispatchers.IO) {
                println("1 ${Thread.currentThread().name}")
                withContext(dispatcherClient) {
                    println("2 ${Thread.currentThread().name}")
                }
                println("3 ${Thread.currentThread().name}")
                withContext(dispatcherClient) {
                    println("4 ${Thread.currentThread().name}")
                }
            }
            job.join()
        }
    }

    @Test
    fun testCancelFinally() {
        runBlocking {
            val job = GlobalScope.launch {
                try {
                    println("Start job")
                    delay(1000)
                    println("End job")
                } finally {
                    println(isActive)
                    delay(1000)
                    println("Finally")
                }
            }
            job.cancelAndJoin()
        }
    }

    @Test
    fun testNonCancellable() {
        runBlocking {
            val job = GlobalScope.launch {
                try {
                    println("Start job")
                    delay(1000)
                    println("End job")
                } finally {
                    withContext(NonCancellable) {
                        println(isActive)
                        delay(1000)
                        println("Finally")
                    }
                }
            }
            job.cancelAndJoin()
        }
    }
}