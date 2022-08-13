package programmer.zaman.now.coroutine

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors

class CoroutineScopeTest {
    /*
    ============= Coroutine Scope ==============

    Coroutine Scope
    Sampai saat ini, kita selalu menggunakan GlobalScope untuk membuat coroutine. Kita belum membahas sama sekali tentang apa itu GlobalScope? GlobalScope sebenarnya adalah salah satu implementasi Coroutine Scope
    Semua coroutine itu sebenarnya dijalankan dari sebuah coroutine scope.
    Function launch dan async yang selama ini kita gunakan, sebenarnya adalah extention function dari coroutine scope
    Secara sederhana, coroutine scope adalah object life cycle nya coroutine.

    Penggunaan Coroutine Scope
    CoroutineScope biasanya digunakan dalam sebuah flow yang saling berhubungan
    Misal saat kita membuka sebuah halaman di mobile, maka kita akan membuat screen, lalu mengambil data ke server, lalu setelah mendapatkanya kita akan menampilkan data tersebut di screen.
    Flow tersebut harus saling terintegrasi, jika misal flow tersebut sukses maka harus sukses semua, jika dibatalkan, maka harus dibatalkan proses selanjutnya
    Hal tersebut jika diibaratkan tiap aktivitas adalah coroutine, maka flow tersebut di simpah dalam sebuah coroutine scope.

    GlobalScope
    Sebelumnya kita selalu menggunakan GlobalScope untuk membuat coroutine
    Sebenarnya penggunaan GlobalScope tidak dianjurkan dalam pembuatan aplikasi
    alasan tidak dianjurkan dikarenakan global scope sifatnya sharing coroutine scope artinya data dari suatu globalScope dapat diakses pada bagian manapun pada program di aplikasi kita
    Hal ini dikarenakan, jika semua coroutine menggunakan GlobalScope, maka secara otomatis akan sharing coroutine scope, hal ini agak menyulitkan saat kita misal ingin membatalkan sebuah flow,
    karena saat sebuah coroutine scope di batalkan, maka semua coroutine yang terdapat di scope tersebut akan dibatalkan

    Jadi penggunaan globalScope hanya boleh digunakan untuk sample saja,pada penerapan aplikasi globalScope tidak diperkenankan
    Pada kenyataan/penerapan sebenarnya pada aplikasi yg kita buat di haruskan membuat coroutine scope sendiri
    Untuk membuat coroutineScope sendiri kita dapat menggunakan function CoroutineScope()

    // Kode : Membuat Coroutine Scope
    @Test
    internal fun testNewScope(){
        val scope=CoroutineScope(Dispatchers.Default)
        scope.Launch{
            delay(2000)
            println("2000:${Date()}")
        }
        scope.Launch{
            delay(1000)
            println("1000:${Date()}")
        }
    }

   // Kode : Membatalkan Coroutine Scope
   @Test
   internal fun testNewScope(){
        val scope=CoroutineScope(Dispatchers.Default)
        scope.launch{
           delay(1000)
           println("1000:${Date()}")
        }
        runBlocking{
            delay(1000)
            scope.cancel()
            delay(3000)
        }
    }
    */

    // Kode : Menjalankan Coroutine Scope
    @Test
    fun testScope() {
        /*
        CoroutineScope bukan sebuah class melainkan sebuah,melainkan sebuah function
        yang mana pada context nya dapat kita diisi menggunakan dispatcher atau yang lainnya selama itu turunan dari coroutineContext
        context pada coroutineScope hanya dapat diisi menggunakan turunan dari CoroutineContext

        Contoh documentasi source code dari CoroutineScope:
        public fun CoroutineScope(context: CoroutineContext): CoroutineScope =
        ContextScope(if (context[Job] != null) context else context + Job())
         */
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            delay(2000)
            println("Run ${Thread.currentThread().name}")
        }

        scope.launch {
            delay(2000)
            println("Run ${Thread.currentThread().name}")
        }

        runBlocking {
            delay(2000)
            println("Done")
        }
        /*
        output:
        Done
        Run DefaultDispatcher-worker-1 @coroutine#2
        Run DefaultDispatcher-worker-3 @coroutine#1

        Alasan penggunaan DefaultDispatcher karena menggunakan Dispatcher.IO pada context di coroutine
         */
    }

    // Kode : Membatalkan Coroutine Scope
    @Test
    fun testScopeCancelable() {
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            delay(2000)
            println("Run ${Thread.currentThread().name}")
        }

        scope.launch {
            delay(2000)
            println("Run ${Thread.currentThread().name}")
        }

        // ketika menggunakan cancel() maka semua coroutine yg ada pada scope sama secara otomatis dibatalkan
        runBlocking {
            delay(1000)
            scope.cancel() // dikarenakan waktu delay pada cancel lebih cepat (delay(1000)) daripada delay pada coroutine (delay(1000)) maka semua coroutine pada scope yg sama akan dibatalkan
            delay(2000)
            println("Done")
        }

        /*
        output:
        Done

        Alasan Done terjadi karena coroutine pada scope sama yg dijalankan sebelumnya akan di cancel sehingga output yg ditampilkan hanya "Done"
         */
    }

    /*
    ================= coroutineScope Function =================

    coroutineScope Function
    Kadang pembuatan coroutine scope itu terlalu kompleks jika hanya untuk kasus-kasus yang sederhana,
    misal saja kita hanya ingin menggabungkan beberapa suspend function, lalu mengembalikan nilai tersebut
    Pada kasus yang sederhana, kita bisa menggunakan coroutineScope function untuk menggabungkan beberapa suspend function
    Saat ada error di coroutine yang terdapat di dalam coroutine scope function tersebut, maka semua coroutine pun akan dibatalkan
     */
    @Test
    fun testScopeCancel() {
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            delay(2000)
            println("Run ${Thread.currentThread().name}")
        }

        scope.launch {
            delay(2000)
            println("Run ${Thread.currentThread().name}")

            coroutineScope {

            }
        }

        runBlocking {
            delay(1000)
            scope.cancel()
            delay(2000)
            println("Done")
        }
    }

    suspend fun getFoo(): Int {
        delay(1000)
        println("Foo ${Thread.currentThread().name}")
        return 10
    }

    suspend fun getBar(): Int {
        delay(1000)
        println("Bar ${Thread.currentThread().name}")
        return 10
    }

    suspend fun getSum(): Int = coroutineScope {
        val foo = async { getFoo() }
        val bar = async { getBar() }
        foo.await() + bar.await()
    }

    suspend fun getSumManual(): Int {
        val scope = CoroutineScope(Dispatchers.IO)
        val foo = scope.async { getFoo() }
        val bar = scope.async { getBar() }
        return foo.await() + bar.await()
    }

    @Test
    fun testCoroutineScopeFunction() {
        val scope = CoroutineScope(Dispatchers.IO)
        val job = scope.launch {
            val result = getSum()
            println("Result $result")
        }

        runBlocking {
            job.join()
        }
    }

    /*
    =============== Coroutine Scope Parent & Child ===============

    Coroutine Scope Parent & Child
    Saat kita membuat sebuah coroutine scope dengan menggunakan function coroutineScope, sebenarnya kita telah membuat child scope dari parent scope nya
    Coroutine scope itu saling berkaitan antara parent dan child nya
    Saat kita membuat child scope, secara otomatis child scope akan menggunakan dispatcher milik parent
    Dan saat kita membatalkan parent scope, maka semua child scope nya pun akan dibatalkan

     */

    // Kode : Dispatcher Child Scope
    @Test
    fun testParentChildDispatcher() {
        val dispatcher = Executors.newFixedThreadPool(10).asCoroutineDispatcher()
        val scope = CoroutineScope(dispatcher)

        val job = scope.launch {
            println("Parent Scope : ${Thread.currentThread().name}") // Parent Scope : pool-1-thread-1 @coroutine#1
            coroutineScope {
                launch {
                    println("Child Scope : ${Thread.currentThread().name}") // Child Scope : pool-1-thread-2 @coroutine#3
                }
            }
        }

        runBlocking {
            job.join()
        }
        /*
        output:
        Parent Scope : pool-1-thread-1 @coroutine#1
        Child Scope : pool-1-thread-2 @coroutine#3

        ParentScope disini adalah scope.lauch dan ChildScope adalah coroutineScope didalam scope.launch
        Berdasarkan output diatas,dapat dilihat bahwa pool thread yg digunakan pada parentScope dan ChildScope itu sama
        Artinya parentScope dan ChildScope sebenarnya menggunakan dispatcher yg sama,artinya dispatcher dari parentScope akan diturunkan kepada childScope
         */
    }

    // Kode : Membatalkan Parent Scope
    // artinya ketika kita membatalkan parentScope maka childScope juga ikut dibatalkan karena dispatcher yg digunakan childScope merupakan turunan dari parentScope
    @Test
    fun testParentChildCancel() {
        val dispatcher = Executors.newFixedThreadPool(10).asCoroutineDispatcher()
        val scope = CoroutineScope(dispatcher)

        val job = scope.launch {
            println("Parent Scope : ${Thread.currentThread().name}")
            coroutineScope {
                launch {
                    delay(2000)
                    println("Child Scope : ${Thread.currentThread().name}")
                }
            }
        }

        runBlocking {
            job.cancelAndJoin()
        }
        /*
        output:
        Parent Scope : pool-1-thread-1 @coroutine#1

        Output yg muncul hanya pada parentScope dikarenakan pada childScope terdapat delay(2000)
        Ketika terjadi delay(2000) pada childScope,maka thread akan mengeksekusi pada bagian lain yaiut bagian cancel
        Sehingga output pada childScope tidak muncul karena program terkena cancel lebih dahulu sebelum waktu delay selesai
         */
    }
}