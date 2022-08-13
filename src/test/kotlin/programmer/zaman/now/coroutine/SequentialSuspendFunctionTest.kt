package programmer.zaman.now.coroutine

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

class SequentialSuspendFunctionTest {

    /*
    ========== Sequential Suspend Function =============

    Suspend Function Tidak Async
    Secara default, sebenarnya sebuah suspend function tidaklah async, saat kita mengakses beberapa suspend function,
    semua suspend function akan dieksekusi secara sequential tidak secara async
    jadi tidak terdapat hubungan ketika mengakses suspend function dengan async
     */

    suspend fun getFoo():Int {
        delay(1000)
        return 10
    }

    suspend fun getBar():Int {
        delay(1000)
        return 10
    }

    // menjalankan suspend function di dalam runBlocking
    @Test
    fun testSequential(){
        runBlocking {
            val time = measureTimeMillis {
                getFoo()
                getBar()
            }
            println("Total time : $time") // output, Total time : 2018
            // jadi ketika kita mengeksekusi dua suspend function secara bersamaan menggunakan runBlocking
            // maka dua suspend function tsb akan dieksekusi secara parallel bukan secara async
            // karena suspend function tidak ada hubungannya dengan async
            // jadi output diatas 2018/2 detik karena djalankan secara sequential atau berurutan tidak secara async
        }
    }

    // menjalankan suspend function di dalam coroutine
    @Test
    fun testSequentialCoroutine(){
        runBlocking {
            val job = GlobalScope.launch {
                val time = measureTimeMillis {
                    getFoo()
                    getBar()
                }
                println("Total time : $time")
                // hasil yang diberikan ketika kita mengakses suspend function di dalam coroutine adalah secara sequential bukan async
            }
            job.join()
        }
    }

    /*
    Concurrent Dengan Launch
    Jadi agar sebuah suspend function bisa berjalan secara concurrent, kita perlu menggunakan function launch ketika memanggil suspend function tersebut
    Hal yang menyulitkan adalah, launch function mengembalikan Job, dan di dalam Job, kita tidak bisa mengembalikan nilai hasil dari coroutine.
    Hal ini bisa dianalogikan bahwa launch itu adalah menjalankan coroutine yang mengembalikan nilai Unit (tidak mengembalikan nilai)
     */
    @Test
    fun testConcurrent(){
        runBlocking {
            val time = measureTimeMillis {
                val job1 = GlobalScope.launch { getFoo() }
                val job2 = GlobalScope.launch { getBar() }

                joinAll(job1, job2)
            }
            println("Total time : $time") // output, Total time : 1073
            // waktu yg diperlukan hanya 1073 atau 1detik karena menggunakan sistem concurency bukan parallel
            // namun jika menggunakan launch kita tidak bisa mendapat value pada coroutine,karena launch tidak mempunyai return value atau void
            // launch mempunyai sifat seperti runnable yakni void atau tidak mempunyai return value untuk dikembalikan
        }
    }

    /*
    ========= Yield Function =============

    Yield function
    Seperti yang pernah kita bahas sebelumnya, bahwa suspend function akan dijalankan secara sequential,
    artinya jika ada sebuah suspend function yang panjang dan lama,Ada baiknya kita beri kesempatan ke suspend function lainnya untuk dijalankan.
    Coroutine berjalan secara concurrent, artinya 1 dispatcher bisa digunakan untuk mengeksekusi beberapa coroutine secara bergantian.
    Saat coroutine kita berjalan,Dan jika kita ingin memberi kesempatan ke coroutine yang lain untuk berjalan, kita bisa menggunakan yield function
    yield function itu bisa di cancel, artinya jika sebuah coroutine telah dibatalkan, maka secara otomatis yield function akan throw CancellationException

     */

    suspend fun runJob(number : Int){
        println("Start job $number in thread ${Thread.currentThread().name}")
        yield()
        println("End job $number in thread ${Thread.currentThread().name}")
    }

    @Test
    fun testYieldFunction() {
        val dispatcher = Executors.newFixedThreadPool(10).asCoroutineDispatcher()
        val scope = CoroutineScope(dispatcher)

        runBlocking {
            scope.launch { runJob(1) }
            scope.launch { runJob(2) }

            delay(2000)
        }
        /*
        output tanpa yield:
        Start job 1 in thread pool-1-thread-1 @coroutine#2
        End job 1 in thread pool-1-thread-1 @coroutine#2
        Start job 2 in thread pool-1-thread-2 @coroutine#3
        End job 2 in thread pool-1-thread-2 @coroutine#3

        output dengan yield:
        Start job 1 in thread pool-1-thread-1 @coroutine#2
        Start job 2 in thread pool-1-thread-2 @coroutine#3
        End job 1 in thread pool-1-thread-3 @coroutine#2
        End job 2 in thread pool-1-thread-4 @coroutine#3
         */
    }
}