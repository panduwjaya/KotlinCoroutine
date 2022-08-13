package programmer.zaman.now.coroutine

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class CoroutineContextTest {

    /*
    ============= Coroutine Context ================

    Coroutine selalu berjalan dibarengi dengan object CoroutineContext
    CoroutineContext adalah sebuah kumpulan data dari CoroutineContext.Element,
    CoroutineContext mirip seperti collection yakni berisi sekumpulan data,namun yg berisikan sekumpulan data dari CoroutineContext.Element
    Fungsi CoroutineContext untuk menyimpan data-data element yg dibutuhkan oleh coroutine
    Jadi data yang terdapat di dalam CoroutineContext.Element adalah data-data yg dibutuhkan oleh coroutine
    Contoh Utama nya adalah Job,data dari job disimpan dalam CoroutineContext karena turunan dari CoroutineContext.Element
    Data dari CoroutineDispatcher juga disimpan didalam CoroutineContext yang akan dibahas di materi tersendiri
    kita juga bisa melakukan data custom dari CoroutineContext namun hal ini jarang sekali dilakukan,biasanya lebih banyak mengakses data yg sudah di sediakan

    penjelasan:
    intinya coroutine context adalah paramter seperti pada function dan seperti constructor pada class,sedangkan pada coroutine disebut context
     */
    @ExperimentalStdlibApi
    @Test
    fun testCoroutineContext() {
        runBlocking {
            val job = GlobalScope.launch {
                val context: CoroutineContext = coroutineContext
                println(context) // [CoroutineId(2), "coroutine#2":StandaloneCoroutine{Active}@33eb12f4, Dispatchers.Default]
                println(context[Job]) // "coroutine#2":StandaloneCoroutine{Active}@33eb12f4
                println(context[CoroutineDispatcher]) // Dispatchers.Default
            }
            job.join()
        }
    }

    /*
    ============= Memberi Nama Coroutine ==============

    Memberi Nama Coroutine
    Selain dispatcher, salah satu coroutine context yang lain adalah CoroutineName
    CoroutineName bisa kita gunakan untuk mengubah nama coroutine sesuai dengan yang kita mau
    Hal ini sangat bermanfaat ketika kita melakukan proses debugging
     */

    // coroutine name parent And child scope
    @Test
    fun testCoroutineName() {
        val scope = CoroutineScope(Dispatchers.IO)
        val job = scope.launch(CoroutineName("parent")) {
            println("Parent run in thread ${Thread.currentThread().name}")
            withContext(CoroutineName("child")){
                println("Child run in thread ${Thread.currentThread().name}")
            }
        }
        runBlocking {
            job.join()
        }
        /*
        output:
        Parent run in thread DefaultDispatcher-worker-1 @parent#1
        Child run in thread DefaultDispatcher-worker-1 @child#1

        Output tanpa coroutine name:
        Parent run in thread DefaultDispatcher-worker-1 @coroutine#1
        Child run in thread DefaultDispatcher-worker-1 @coroutine#1

        Jika tidak menggunakan Coroutine Name maka yg ditampilkan berupa Default Name
         */
    }

    // coroutine name parent scope
    @Test
    fun testCoroutineNameContext() {
        val scope = CoroutineScope(Dispatchers.IO + CoroutineName("test"))
        val job = scope.launch {
            println("Parent run in thread ${Thread.currentThread().name}")
            withContext(Dispatchers.IO){
                println("Child run in thread ${Thread.currentThread().name}")
            }
        }
        runBlocking {
            job.join()
        }
        /*
        output:
        Parent run in thread DefaultDispatcher-worker-1 @test#1
        Child run in thread DefaultDispatcher-worker-1 @test#1

        Output ditampilkan mempunyai Coroutine Name yg sama
        Hal tersebut dapat terjadi karena Coroutine Name scope nya mengikuti dari parent coroutine nya
        Sehingga Coroutine Name pada parent dan child itu sama
         */
    }

    /*
    ========= Menggabungkan Context Element ===========

    Menggabungkan Context Element
    Seperti yang pernah dibahas di materi CoroutineContext
    CoroutineContext adalah kompulan dari Element-Element, contoh turunannya adalah Job, CoroutineDispatcher dan yang terakhir yang sempat kita bahas adalah CoroutineName
    Tanda plus (+) pada coroutine context tersebut disebut method plus,yang berfungsi untuk menggabungkan beberapa context element secara sekaligus
    CoroutineContext memiliki method plus, sehingga sebenarnya kita bisa menggabungkan beberapa context element secara sekaligus, misal Dispatcher dan CoroutineName misalnya

    contoh:
    1). val scope = CoroutineScope(Dispatchers.IO + CoroutineName("test")) = menggabungkan 2 buah context element menggunakan method plus

    EXAMPLE:
    @Test
    fun testCoroutineElements() {
        val dispatcher = Executors.newFixedThreadPool(10).asCoroutineDispatcher()
        val scope = CoroutineScope(Dispatchers.IO + CoroutineName("test")) // menggabungkan context element
        val job = scope.launch(CoroutineName("parent") + dispatcher) { // menggabungkan context element
            println("Parent run in thread ${Thread.currentThread().name}")
            withContext(CoroutineName("child") + Dispatchers.IO){
                println("Child run in thread ${Thread.currentThread().name}")
            }
        }
        runBlocking {
            job.join()
        }
    }
     */
    @Test
    fun testCoroutineElements() {
        val dispatcher = Executors.newFixedThreadPool(10).asCoroutineDispatcher()
        val scope = CoroutineScope(Dispatchers.IO + CoroutineName("test"))
        val job = scope.launch(CoroutineName("parent") + dispatcher) {
            println("Parent run in thread ${Thread.currentThread().name}")
            withContext(CoroutineName("child") + Dispatchers.IO){
                println("Child run in thread ${Thread.currentThread().name}")
            }
        }
        runBlocking {
            job.join()
        }
        /*
        output:
        Parent run in thread pool-1-thread-1 @parent#1
        Child run in thread DefaultDispatcher-worker-1 @child#1
         */
    }
}