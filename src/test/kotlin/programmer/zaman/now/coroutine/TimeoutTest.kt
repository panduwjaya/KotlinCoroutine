package programmer.zaman.now.coroutine

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import java.util.*

class TimeoutTest {
    /*
    =============== timeOut ===================

    Menggunakan Timeout
    Kadang kita ingin sebuah coroutine berjalan tidak lebih dari waktu yang telah ditentukan
    Sebenarnya kita bisa melakukan hal tersebut secara manual, dengan cara menjalankan 2 job, dimana job ke dua akan membatalkan job pertama jika job pertama terlalu lama
    Namun hal ini tidak perlu kita lakukan lagi, terdapat function withTimeout untuk melakukan hal tersebut.
    Jika terjadi timeout melebihi waktu yang telah kita tentukan, maka secara otomatis function withTimeout akan thro TimeoutCancellationException
     */

    // Kode : Menggunakan Timeout
    @Test
    fun testTimeout(){
        runBlocking {
            val job = GlobalScope.launch {
                println("Start Coroutine")
                // apabila coroutine lebih dari 5 detik maka program akan cancel,karena TimeoutCancellationException
                withTimeout(5000){
                    // namun karena pengulangan sebanyak 100x dengan delay 1detik,maka tidak mungkin coroutine kurang dari 5 detik
                    // waktu yg dibutuhkan coroutine adalah 100detik yg mana lebih dari 5 detik
                    // sehingga akan terkena TimeoutCancellationException
                    repeat(100){
                        delay(1000)
                        println("$it ${Date()}")
                    }
                }
                println("Finish Coroutine")
            }
            job.join()
        }
    }

    /*
    Timeout Tanpa Membatalkan Coroutine
    withTimeout akan throw TimeoutCancellationException, dimana itu adalah turunan dari CancellationException
    Hal ini berakibat coroutine akan berhenti karena kita throw exception
    Jika ada kasus dimana kita tidak ingin menghentikan coroutine-nya, kita bisa menggunakan function withTimeoutOrNull
    Function withTimeoutOrNull dimana ini tidak akan throw exception, hanya mengembalikan null jika terjadi timeout
     */

    // Kode : Timeout or Null
    @Test
    fun testTimeoutOrNull(){
        runBlocking {
            val job = GlobalScope.launch {
                println("Start Coroutine")
                withTimeoutOrNull(500){
                    repeat(100){
                        delay(1000)
                        println("$it ${Date()}")
                    }
                }
                println("Finish Coroutine")
            }
            job.join()
        }
    }

}