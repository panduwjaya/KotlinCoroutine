package programmer.zaman.now.coroutine

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test



class JobTest {

//    =================== JOB =======================
    /*
    Job
    Saat sebuah coroutine dijalankan menggunakan function launch, sebenarnya function tersebut mengembalikan sebuah object Job
    Dengan object Job, kita bisa menjalankan, membatalkan atau menunggu sebuah coroutine
    **untuk informasi lebih lanjut dapat membaca dokumentasi dari job (alt+klik)
    */

    // menjalankan launch di dalam runBlocking
    // kekurangan menjalankan launch di dalam runBlocking adalah process pada launch akan tetap berjalan
    // jadi launch tidak terkena efek blocking dari function runBlocking
    @Test
    fun testJob(){
        runBlocking {
            GlobalScope.launch {
                delay(2000)
                println("Coroutine Done ${Thread.currentThread().name}")
            }
        }
    }

    // menjalankan job menggunakan lazy
    // jadi ketika tidak dipanggil maka coroutine tsb tidak dijalankan
    @Test
    fun testJobLazy(){
        runBlocking {
            val job: Job = GlobalScope.launch(start = CoroutineStart.LAZY) {
                delay(2000)
                println("Coroutine Done ${Thread.currentThread().name}")
            }
            job.start() // jika tidak dipanggil menggunakan start() maka coroutine tsb tidak akan di eksekusi

            delay(3000)
        }
    }

    // menunggu job menggunakan join
    // jadi dengan join secara otomatis eksekusi dari program tsb akan ditunggu hingga selesai tanpa menggunakan delay()
    // jadi kita tidak perlu men-set lama delay pada program sampai eksekusi program selesai
    @Test
    fun testJobJoin(){
        runBlocking {
            val job: Job = GlobalScope.launch {
                delay(2000)
                println("Coroutine Done ${Thread.currentThread().name}")
            }
            job.join() // cukup menggunakan join()
        }
    }

    // membatalkan job
    // dengan menggunakan cancel() maka seluruh proses coroutine yg sedang berjalan akan ter-cancel
    // sehingga tidak menimbulkan output pada program coroutine
    @Test
    fun testJobCancel(){
        runBlocking {
            val job: Job = GlobalScope.launch {
                delay(2000)
                println("Coroutine Done ${Thread.currentThread().name}")
            }
            job.cancel()

            delay(3000)
        }
    }

//    ================= joinAll Function ======================
    /*
    joinAll Function
    Kadang kita akan membuat coroutine lebih dari satu sekaligus
    Untuk menunggu semua Job coroutine selesai berjalan kita bisa menggunakan join() function
    Namun jika kita panggil satu-satu tiap Job coroutine nya, akan sangat mengganggu sekali
    Kotlin menyediakan joinAll(jobs) function untuk menunggu semua job selesai
     */
    @Test
    fun testJobJoinAll(){
        runBlocking {
            val job1: Job = GlobalScope.launch {
                delay(1000)
                println("Coroutine Done ${Thread.currentThread().name}")
            }
            val job2: Job = GlobalScope.launch {
                delay(2000)
                println("Coroutine Done ${Thread.currentThread().name}")
            }
            joinAll(job1, job2)
        }
    }

}