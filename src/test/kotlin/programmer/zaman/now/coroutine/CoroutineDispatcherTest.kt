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

    /*
    ============= Membuat Coroutine Dispatcher ===============

    Membuat Coroutine Dispatcher
    Saat membuat aplikasi, kadang kita ingin flexible menentukan thread yang akan kita gunakan untuk  menjalankan coroutine
    Misal, kita ingin membedakan thread untuk layer web, layer http client, dan lain-lain
    jadi disini tujuan kita adalah menentukan tugas setiap Thread itu masing masing,sehingga setiap Thread mempunyai tugas nya sendiri
    Oleh karena ini, membuat Coroutine Dispatcher sendiri sangat direkomendasikan.
    Untuk membuat Coroutine Dispatcher secara manual, kita bisa melakukannya dengan cara menggunakan ExecutorService
    untuk merubah dari ExecutorService ke CoroutineDispatcher kita bisa menggunakan function asCoroutineDispatcher()

     */

    @Test
    fun testExecutorService() {
        val dispatcherService = Executors.newFixedThreadPool(10).asCoroutineDispatcher()
        val dispatcherWeb = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

        runBlocking {
            val job1 = GlobalScope.launch(dispatcherService) {
                println("Job 1 : ${Thread.currentThread().name}")
            } // Job 1 : pool-1-thread-1 @coroutine#2 , artinya job 1 berjalan di ThreadPool 1
            val job2 = GlobalScope.launch(dispatcherWeb) {
                println("Job 2 : ${Thread.currentThread().name}")
            } // Job 2 : pool-2-thread-1 @coroutine#3 , artinya job 2 berjalan di ThreadPool 2 yg mana job 1 dan job 2 berjalan di ExecutorService berbeda atau Dispatcher yg berbeda
            joinAll(job1, job2)
        }
        /*
        output:

        Job 2 : pool-2-thread-1 @coroutine#3
        Job 1 : pool-1-thread-1 @coroutine#2
        BUILD SUCCESSFUL in 7s
         */
    }

    /*
    =============== withContext Function =================

    Sebelumnya kita sudah tahu, bahwa ternyata saat kita melakukan delay(), suspend function tersebut akan di trigger di thread yang berbeda.
    Bagaimana caranya jika kita ingin menjalankan code program kita dalam coroutine di thread yang berbeda dengan thread coroutine awalnya?
    Untuk melakukan itu, kita bisa menggunakan function withContext()
    Function withContext() sebenarnya bisa kita gunakan untuk mengganti CoroutineContext, namun karena CoroutineDispatcher adalah turunan CoroutineContext,
    jadi kita bisa otomatis mengganti thread yang akan digunakan di coroutine menggunakan function withContext()

    *fungsi:
    fungsi dari withContext() adalah untuk melakukan perubahan Thread dan menentukan Thread mana yg digunakan pada eksekusi coroutine
     */
    @Test
    fun testWithContext() {
        val dispatcherClient = Executors.newFixedThreadPool(10).asCoroutineDispatcher()
        runBlocking {
            val job = GlobalScope.launch(Dispatchers.IO) {
                // Thread yg digunakan pada coroutine bagian ini berasal dari Dispathcer.IO
                println("1 ${Thread.currentThread().name}") // output, 1 DefaultDispatcher-worker-1 @coroutine#2
                withContext(dispatcherClient) {
                    // Thread yg digunakan pada coroutine bagian ini berasal diambil dari dispatcherClient
                    println("2 ${Thread.currentThread().name}") // output, 2 pool-1-thread-1 @coroutine#2
                }
                // Thread yg digunakan pada coroutine bagian ini berasal dari Dispathcer.IO
                println("3 ${Thread.currentThread().name}") // output, 3 DefaultDispatcher-worker-1 @coroutine#2
                withContext(dispatcherClient) {
                    // Thread yg digunakan pada coroutine bagian ini berasal diambil dari dispatcherClient
                    println("4 ${Thread.currentThread().name}") // ouput, 4 pool-1-thread-2 @coroutine#2
                }
            }
            job.join()
            /*
            output:

            1 DefaultDispatcher-worker-1 @coroutine#2
            2 pool-1-thread-1 @coroutine#2
            3 DefaultDispatcher-worker-1 @coroutine#2
            4 pool-1-thread-2 @coroutine#2
            BUILD SUCCESSFUL in 47s
             */
        }
    }

    /*
    ================ Non Cancellable Context =================

    Check isActive di Finally
    Sebelumnya kita tahu bahwa setelah coroutine di cancel, dan jika kita ingin melakukan sesuatu, kita bisa menggunakan block try-finally
    Namun dalam block finally, kita tidak bisa menggunakan suspend method yang mengecek isActive, karena otomatis akan bernilai false, dan otomatis batal

    * Kode : Check isActive di Finally
    @Test
    fun testCancelFinally(){
    runBlocking{
        val job=GlobalScope.Launch{
             try{
                 println("Hello$(Date())")
                 delay(2008)
                 println("World$(Date())")
             }finally{
                 println(isActive)
                 delay(1000)
                 println("Finally")
             }
        }
        job.cancelAndJoin()
    }

    Non Cancellable Context
    Jika kita butuh memanggil suspend function yang mengecek isActive di block finally, dan berharap tidak dibatalkan eksekusinya, maka kita bisa menggunakan NonCancellable
    NonCancellable adalah coroutine context yang mengoverride nilai-nilai cancellable sehingga seakan-akan coroutine tersebut tidak di batalkan

    * Kode : Non Cancellable Context
    @Test
    fun testNonCancellable(){
    runBlocking{
        val job GlobalScope.Launch{
            try{
                 println("Hello${Date()}")
                 delay(2008)
                println("World${Date()}")
            }finally{
                withContext(NonCancellable){
                     println(isActive)
                     delay(1000)
                     println("Finally")
                }
            }
        }
        job.cancelAndJoin()
    }
     */

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