package programmer.zaman.now.coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors

class LockingTest {
    /*
    ======== Mutex =========

    Shared Mutable State
    Saat kita belajar Kotlin Collection, kita sudah tau tentang Immutable dan Mutable
    Saat menggunakan coroutine, sangat disarankan untuk menggunakan data Immutable, apalagi jika data tersebut di sharing ke beberapa coroutine
    Dikarenakan apabila data tersebut mutable(dapat diubah),jika terdapat perubahan data pada salah satu coroutine maka akan merubah data pada semua coroutine
    Serta akan berpengaruh pada coroutine lainnya
    Hal ini agar datanya aman, karena tidak bisa diubah oleh coroutine lain, jadi tidak akan terjadi problem race condition
    Problem Race condition merupakan kondisi dimana sebuah data diubah secara bersama-sama oleh beberapa coroutine sekaligus
    Namun, bagaimana jika ternyata kita memang butuh sharing mutable data di beberapa coroutine secara sekaligus?

    catatan:
    Jadi dapat dikatakan bahwa Problem Race condition merupakan suatu kondisi terdapat rebutan dalam sistem pengubahan sebuah data
     */

    // kode race condition coroutine
    @Test
    fun testRaceCondition() {
        var counter: Int = 0
        val dispatcher = Executors.newFixedThreadPool(10).asCoroutineDispatcher()
        val scope = CoroutineScope(dispatcher)

        repeat(100) {
            scope.launch {
                repeat(1000) {
                    counter++
                }
            }
        }

        runBlocking {
            delay(5000)
            println("Total Counter : $counter")
        }
        /*
        output:
        Total Counter : 76378

        catatan:
        Output yang seharusnya ditampilkan adalah 100.000 bukan 76.378
        nilai output yg seharusnya ditampilkan 100.000 dapat diperoleh melalui repeat coroutine sebanyak 100kali dengan jumlah satu kali repeat adalah 1000kali
        Namun hal tersebut tidak dapat terjadi dikarenakan Problem Race Condition
        Dikarenakan coroutine tsb dijalankan secara bersama-sama,kemungkinan disaat yg bersamaan terdapat coroutine yang mengUpdate/repeat value yg sama
        Sehingga terdapat value ganda pada counter yg berasal dari beberapa coroutine,sehingga hasil output tsb tidak mencapai 100.000 namun 76.378 karena terdapat duplicate value

        solusi:
        Solusi dari permasalahan proble race condition ini dapat menggunakan fitur Mutex
         */
    }

    /*
    Mutex
    Mutex (Mutual exclusion) adalah salah satu fitur di Kotlin Coroutine untuk melakukan proses locking
    Dengan menggunakan mutex, kita bisa pastikan bahwa hanya ada 1 coroutine yang bisa mengakses kode tersebut, code coroutine yang lain akan menunggu sampai coroutine pertama selesai

    Konsep Mutex (ctrl + klik):

    lock(owner) // pada tahap ini artinya coroutine dilock sehingga hanya satu coroutine saja yg mengakses value
    try {
        return action()
    } finally {
        unlock(owner) // pada tahap ini lock dibuka dan coroutine lain dapat masuk kembali ke bagian lock untuk mengakses value
    }

    catatan:
    Artinya setiap value yg ada tidak akan diulang,dikarenakan setiap coroutine hanya mendapat jatah 1 value saja tidak lebih
    Jadi setiap akses pada value dilakukan secara bergantian,jadi satu value diakses oleh beberapa coroutine

    Kekurangan:
    Kekurangan yang kita dapatkan adalah kode akan berjalan lebih lama dibandingkan tidak menggunakan mutex
    Hal tersebut dapat terjadi karena setiap coroutine hanya dapat bisa mengakses value secara satu per satu tidak secara bersamaan
    Namun,dibalik hal tersebut menggunakan mutex lebih aman dibanding tidak menggunakan mutex
     */

    // kode locking Test Mutex
    @Test
    fun testMutex() {
        var counter: Int = 0
        val dispatcher = Executors.newFixedThreadPool(10).asCoroutineDispatcher()
        val scope = CoroutineScope(dispatcher)
        val mutex = Mutex()

        repeat(100) {
            scope.launch {
                repeat(1000) {
                    mutex.withLock { // pada scope ini,setiap coroutine hanya dapat mengakses satu value sehingga tidak terdapat value ganda
                        counter++
                    }
                }
            }
        }

        runBlocking {
            delay(5000)
            println("Total Counter : $counter")
        }
        /*
        output:
        Total Counter : 100000

        catatan:
        Output yang ditampilkan sesuai dengan perkiraan yaitu 100.000 tidak lebih dan tidak kurang
        Hal tersebut dapat terjadi dikarenakan tidak terdapat coroutine yang mengUpdate/repeat value yg sama
         */
    }

    /*
    ========= Semaphore ==========

    Sama seperti Mutex, Semaphore juga digunakan sebagai object untuk locking
    Namun yang membedakan, pada Mutex, kita hanya memperbolehkan 1 coroutine yang bisa mengakses nya pada satu waktu
    Namun pada Semaphore, kita bisa menentukan berapa jumlah corotine yang boleh mengakses nya pada satu waktu
    Untuk menentukan jumlah coroutine yg dapat mengakses pada semaphore menggunakan permits

    catatan:
    Kekurangan pada semaphore adalah semakin banyak jumlah coroutine yg digunakan dalam satu waktu maka data yg dihasilkan semakin tidak akurat
    Kelebihan yg didapat jika menggunakan permits dalam jumlah besar adalah tingkat kecepatan pemrosesan data lebih cepat,namun data yg didapat tidak akurat
     */
    @Test
    fun testSemaphore() {
        var counter: Int = 0
        val dispatcher = Executors.newFixedThreadPool(10).asCoroutineDispatcher()
        val scope = CoroutineScope(dispatcher)
        val semaphore = Semaphore(permits = 2) // jumlah coroutine yg dapat mengakses dalam satu waktu adalah 2 buah

        repeat(100) {
            scope.launch {
                repeat(1000) {
                    semaphore.withPermit {
                        counter++
                    }
                }
            }
        }

        runBlocking {
            delay(5000)
            println("Total Counter : $counter")
        }
        /*
        output semaphore dengan permits 1:
        Total Counter : 100000

        output semaphore dengan permits 2:
        Total Counter : 99856

        Berdasarkan hasil output antara penggunaan permits 1 dan permits 2 dapat terlihat bahwa pada permits 2 tingkat keakuratan data nya sangat rendah
        Karena output yg seharusnya dihasilkan dari perulangan 100 coroutine sebanyak 100 repeat adalah 100.000
        Namun pada output semaphore dengan permits 2 output nya adalah 99856 yg dapat dilihat melenceng dan tingkat keakuratan nya sangat rendah
         */
    }
}