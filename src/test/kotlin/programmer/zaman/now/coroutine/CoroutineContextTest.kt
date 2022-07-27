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
    }

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
    }

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
    }
}