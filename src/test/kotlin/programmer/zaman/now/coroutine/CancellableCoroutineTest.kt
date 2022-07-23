package programmer.zaman.now.coroutine

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import java.util.*

class CancellableCoroutineTest {

    /*
    =========== Cancellable Coroutine =============

    Membatalkan Coroutine
    Sebelumnya kita sudah tahu bahwa Job bisa kita batalkan menggunakan function cancel
    Membatalkan coroutine kadang diperlukan, misal ketika kode program di coroutine terlalu lama
    Semua function yang ada di package kotlinx.coroutines bisa dibatalkan.
    Namun, jika dalam kode program kita, kita tidak mengecek status cancel, maka coroutine yang kita buat tidak akan bisa dibatalkan
     */
    @Test
    fun testCanNotCancel() {
        runBlocking {
            val job = GlobalScope.launch {
                println("Start coroutine ${Date()}")
                Thread.sleep(2000)
                println("End coroutine ${Date()}")
            }
            job.cancel() // ini tidak berfungsi pada Thread.sleep,hanya berfungsi pada delay()
            delay(3000)
        }
    }

    /*
    Agar Coroutine Bisa Dibatalkan
    Untuk mengecek apakah coroutine masih aktif atau tidak (selesai / dibatalkan), kita bisa menggunakan field isActive milik CoroutineScope
    Untuk menandakan bahwa coroutine dibatalkan, kita bisa throw CancellationException

    terdapat function bawaan kotlin yaitu ensureActive()
    yang mempunyai fungsi seperti dibawah ini:
    if (!isActive) throw CancellationException()
                println("Start coroutine ${Date()}")
     */
    @Test
    fun testCancellable() {
        runBlocking {
            val job = GlobalScope.launch {
                if (!isActive) throw CancellationException()
                println("Start coroutine ${Date()}")

                ensureActive()
                Thread.sleep(2000)

                ensureActive() // disini baru coroutine dapat ditangkap dalam posisi inActive,karena Thread.sleep()
                println("End coroutine ${Date()}")
            }
            job.cancel()
            delay(3000)
            /*
            output -> Start coroutine Sat Jul 23 23:36:27 ICT 2022
            alasan output yg dihasilkan hanya Start coroutine Sat Jul 23 23:36:27 ICT 2022
            karena ketika suatu coroutine masih active hal tersebut tidak bisa di cancel
            harus menunggu pada keadaan inActive seperti saat Thread.sleep() atau delay()
            baru saat keadaan inActive seperti itu baru bisa menggunakan CancellationException()
             */
        }
    }

    /*
    ============ Setelah Coroutine di Cancel ==============

    Standard coroutine adalah, ketika sebuah coroutine dibatalkan, maka kita perlu throw CancellableException
    Namun jika kita ingin melakukan sesuatu setelah sebuah coroutine dibatalkan,kita bisa menggunakan block try-finally
    ketika terjadi cancel pada Thread.sleep() atau delay() maka secara otomatis akan dialihkan bagian finally
    walaupun tidak melakukan cancel pada program,apabila terdapat finally pada program maka sesuatu yg ada didalam finally tetap di eksekusi
    penggunaan cancelAndJoin merupakan gabungan antara Cancel() dan Join() sehingga kita bisa menggunakan dua function tsb bersamaan
     */
    @Test
    fun testCancellableFinally() {
        runBlocking {
            val job = GlobalScope.launch {
                try {
                    println("Start coroutine ${Date()}")
                    delay(2000)
                    println("End coroutine ${Date()}")
                } finally {
                    println("Finish")
                }
            }
            job.cancelAndJoin()
        }
    }
}