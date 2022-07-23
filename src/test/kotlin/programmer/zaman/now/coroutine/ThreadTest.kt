package programmer.zaman.now.coroutine

import org.junit.jupiter.api.Test
import java.util.*
import kotlin.concurrent.thread

//membahas dari Thread utama - Multiple Thread

class ThreadTest {
    /*
    Thread Utama
    Saat kita menjalankan sebuah process (aplikasi) Kotlin di JVM, secara otomatis proses tersebut akan jalan di sebuah thread utama
    Thread utama tersebut bernama main thread (JVM)
    Saat kita menjalankan process JUnit, JUnit pun berjalan di thread tersendiri
    Begitu juga jika kita  membuat aplikasi kotlin Android, aplikasi tersebut akan berjalan di sebuah thread

    contoh kode program untuk thread utama:
    fun main(){
        val thredName = Thread.currentThread().name
        println(Thread name) // output, thread main
    }
    */

    /*
    jadi kita bisa mengetahui diposisi thread mana kita berada menggunakan (Thread.currentThread().name)
    setiap thread mempunyai namanya masing2,pada thread dibawah output yg dikeluarkan adalh Test worker
    artinya program unit test tersebut berjalan pada thread Test worker
    karena setiap unit test pasti berjalan pada thread yang bernama test worker
    */
    @Test
    fun testThreadName() {
        val threadName = Thread.currentThread().name
        println("Running in thread $threadName") //output, Running in thread Test worker
    }

    /*
    Membuat Thread
    Kotlin menggunakan Java Thread, sehingga pembuatan Thread di Kotlin sama seperti pembuatan Thread di Java
    Untuk membuat Thread, kita bisa menggunakan interface Runnable sebagai kode program yang akan dieksekusi, lalu menggunakan method Thread.start() untuk menjalankan Thread tersebut
    Ingat, Thread akan berjalan secara paralel, sehingga tidak akan ditunggu oleh Thread utama
    Kotlin memiliki helper function bernama thread() jika kita ingin membuat thread lebih singkat dan mudah
    Date() berfungsi untuk menunjukan waktu saat thread dijalankan
     */
    @Test
    fun testNewThread() {
        // contoh java thread,pada java thread apabila tidak menggunakan thread.start() maka thread tsb tidak berjalan
//        val runnable = Runnable {
//            println(Date())
//            Thread.sleep(2000)
//            println("Finish : ${Date()}")
//        }
//
//        val thread = Thread(runnable)
//        thread.start()

        // contoh penerapan kotlin thread,pada kotlin thread kita tidak perlu manual melakukan start pada thread seperti pada java thread
        thread(start = true) {
            println(Date())
            Thread.sleep(2000)
            println("Finish : ${Date()}")
        }

        println("MENUNGGU SELESAI")
        Thread.sleep(3000)
        println("SELESAI")
    }

    /*
    Multiple Thread
    Tidak ada batasan dalam membuat Thread
    Kita bisa membuat Thread sebanyak yang kita mau
    Semua Thread akan berjalan sendiri-sendiri secara paralel
     */
    @Test
    fun testMultipleThread() {
        // thread pertama
        val thread1 = Thread(Runnable {
            println(Date())
            Thread.sleep(2000)
            println("Finish Thread 1 : ${Thread.currentThread().name} : ${Date()}")
        })

        //thread kedua
        val thread2 = Thread(Runnable {
            println(Date())
            Thread.sleep(2000)
            println("Finish Thread 2 : ${Thread.currentThread().name} : ${Date()}")
        })

        thread1.start()
        thread2.start()

        println("MENUNGGU SELESAI")
        Thread.sleep(3000)
        println("SELESAI")
    }
}