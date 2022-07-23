package programmer.zaman.now.coroutine

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.concurrent.thread

/*
Membuat Coroutine
Coroutine tidak bisa berjalan sendiri, dia perlu berjalan di dalam sebuah Scope.
Salah satu scope yang bisa kita gunakan adalah GlobalScope (masih banyak scope yang ada, dan akan kita bahas nanti dimateri tersendiri)
Untuk membuat coroutine, kita bisa menggunakan method launch() yg mana launch tsb terdapat di dalam GlobalScope
Dan di dalam coroutine, kita bisa memanggil suspend function
selain kita bisa memanggil suspend func di dalam suspend func,
kita juga bisa memanggil suspend function di dalam coroutine menggunakan global scope dengan launch yg terdapat di dalamnya
 */

class CoroutineTest {

    suspend fun hello() {
        delay(1_000)
        println("Hello World")
    }

    /*
    penggunaan delay pada global scope diperlukan agar program tidak langsung berhenti
    ketika ketika kita memanggil suspend func hello maka harus delay selama satu detik
    maka apabila pada global scope tidak kita berikan delay lebih dari satu detik maka suspend func hello tidak akan dieksekusi
    karena pada async apabila program tsb diam maka akan dilewatkan dalam proses eksekusinya
     */
    @Test
    fun testCoroutine() {
        GlobalScope.launch {
            hello()
        }

        println("MENUNGGU")
        runBlocking {
            delay(2_000)
        }
        println("SELESAI")
    }

    /*
    =============== Coroutine Sangat Ringan ======================
    Seperti yang sebelumnya dibahas, coroutine itu ringan dan cepat, sehingga saat kita membuat coroutine dalam jumlah besar,
    ini tidak akan berdampak terlalu besar dengan memory yang kita gunakan
    Sekarang kita akan coba bandingkan membuat thread dan coroutine dalam jumlah banyak
     */

    // Kode : Membuat Thread Banyak
    // hasil yg didapatkan ketika kita menggunakan banyak thread secara bersamaan atau parallel maka akan error
    @Test
    fun testThread() {
        repeat(10000) {
            thread {
                Thread.sleep(1_000)
                println("Done $it : ${Date()}")
            }
        }

        println("Waiting")
        Thread.sleep(3_000)
        println("Finish")
    }

    // Kode : Membuat Coroutine Banyak
    // dengan menggunakan coroutine kita bisa menjalankan concurency dan parallel secara bersamaan
    // data dengan jumlah banyak akan mudah di selesaikan dengan menggunakan coroutine
    @Test
    fun testCoroutineMany() {
        repeat(100_000) {
            GlobalScope.launch {
                delay(1_000)
                println("Done $it : ${Date()} ${Thread.currentThread().name}")
            }
        }

        println("Waiting")
        runBlocking {
            delay(3_000)
        }
        println("Finish")
    }

    @Test
    fun testParentChild() {
        runBlocking {
            val job = GlobalScope.launch {
                launch {
                    delay(2000)
                    println("Child 1 Done")
                }
                launch {
                    delay(4000)
                    println("Child 2 Done")
                }
                delay(1000)
                println("Parent Done")
            }

            job.join()
        }
    }

    @Test
    fun testParentChildCancel() {
        runBlocking {
            val job = GlobalScope.launch {
                launch {
                    delay(2000)
                    println("Child 1 Done")
                }
                launch {
                    delay(4000)
                    println("Child 2 Done")
                }
                delay(1000)
                println("Parent Done")
            }

            job.cancelChildren()
            job.join()
        }
    }

    @Test
    fun testAwaitCancellation() {
        runBlocking {
            val job = launch {
                try {
                    println("Job start")
                    awaitCancellation()
                } finally {
                    println("Cancelled")
                }
            }

            delay(5000)
            job.cancelAndJoin()
        }
    }
}