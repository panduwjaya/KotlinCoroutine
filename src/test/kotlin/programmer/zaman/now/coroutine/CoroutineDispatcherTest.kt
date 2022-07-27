package programmer.zaman.now.coroutine

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors

class CoroutineDispatcherTest {

    /*
    Coroutine Dispatcher
    Selain ada Job di dalam CoroutineContext, ada juga object CoroutineDispatcher.
    CoroutineDispatcher digunakan untuk menentukan thread mana yang bertanggung jawab untuk mengeksekusi coroutine
    Secara default sudah ada setting default dispatcher, namun kita bisa menggantinya jika kita mau artinya kita bisa melakukan custom dispatcher

    Dispatchers
    Ada object Dispatchers yang bisa kita gunakan untuk mengganti CoroutineDispatcher
    Dispatchers.Default, ini adalah default dispatcher, isinya minimal 2 thread, atau sebanyak jumlah cpu (mana yang lebih banyak). Dispatcher ini cocok untuk proses coroutine yang cpu-bound
    Dispatcher.IO, ini adalah dispatcher yang berisikan thread sesuai dengan kebutuhan, ketika butuh akan dibuat, ketika sudah tidak dibutuhkan, akan dihapus, mirip cache thread pool di executor service. Dispatcher ini akan sharing thread dengan Default dispatcher
    Dispatchers.Main, ini adalah dispatchers yang berisikan main thread UI, cocok ketika kita butuh running di thread main seperti di Java Swing, JavaFX atau Android. Untuk menggunakan ini, kita harus menambah library ui tambahan
     */

    @Test
    fun testDispatcher() {
        runBlocking {
            println("runBlocking ${Thread.currentThread().name}")
            val job1 = GlobalScope.launch(Dispatchers.Default) {
                println("Job 1 ${Thread.currentThread().name}")
            }
            val job2 = GlobalScope.launch(Dispatchers.IO) {
                println("Job 2 ${Thread.currentThread().name}")
            }
            joinAll(job1, job2)
        }
    }

    @Test
    fun testUnconfined() {
        runBlocking {
            println("runBlocking ${Thread.currentThread().name}")

            GlobalScope.launch(Dispatchers.Unconfined) {
                println("Unconfined : ${Thread.currentThread().name}")
                delay(1000)
                println("Unconfined : ${Thread.currentThread().name}")
                delay(1000)
                println("Unconfined : ${Thread.currentThread().name}")
            }
            GlobalScope.launch {
                println("Confined : ${Thread.currentThread().name}")
                delay(1000)
                println("Confined : ${Thread.currentThread().name}")
                delay(1000)
                println("Confined : ${Thread.currentThread().name}")
            }

            delay(3000)
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