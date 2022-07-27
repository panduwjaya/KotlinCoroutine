package programmer.zaman.now.coroutine

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

class AsyncTest {

    /*
    ============ Async Function ============

    Async Function
    Untuk membuat coroutine, kita tidak hanya bisa menggunakan function launch, ada function async yang bisa kita gunakan juga untuk membuat coroutine
    Berbeda dengan launch function yang mengembalikan Job, async function mengembalikan Deferred
    Deferred adalah turunan dari Job, yang membedakan adalah,Deferred membawa value hasil dari async function
    Deferred adalah kebalikan dari launch yaitu mampu mengembalikan value atau return value bukan void seperti launch
    Deferred itu mirip konsep Promise atau Future, dimana datanya akan ada nanti
    Jika kita ingin menunggu data di Deferred sampai ada, kita bisa menggunakan method await()

    **fungsi:
    kita kita mengingkan coroutine yg mempunyai return value kita bisa menggunakan Deferred
     */
    suspend fun getFoo():Int {
        delay(1000)
        return 10
    }

    suspend fun getBar():Int {
        delay(1000)
        return 10
    }

    @Test
    fun testAsync(){
        runBlocking {
            val time = measureTimeMillis {
                /*
                untuk menentukan type data pada Deferred<T> adalah sesuai dengan return value dari suspend function yg dipanggil
                dikarenkan return value pada getFoo dan getBar() adalah Int maka type data pada Deferred adalah Int
                 */
                val foo: Deferred<Int> = GlobalScope.async { getFoo() }
                val bar: Deferred<Int> = GlobalScope.async { getBar() }

                // await mempunyai sifat seperti future yakni akan menampilkan hasil dari value ketika value tsb sudah ada
                val result = foo.await() + bar.await() // ini akan berjalan secara async bukan sequential
                println("Result : $result") // menunjukan return value dari getFoo() dan getBar(),output "Result : 20"
            }
            println("Total time : $time") // output "Total time : 1085",karena dijalankan secara async
        }
    }

    /*
    ============== awaitAll Function ===============

    awaitAll Function
    Pada materi sebelumnya kita membuat beberapa async coroutine, lalu kita menggunakan await function untuk menunggu hasil nya
    Pada job, tersedia joinAll untuk menunggu semua launch coroutine selesai
    Kotlin juga menyediakan awaitAll untuk menunggu semua Deferred selesai mengembalikan value
    awaitAll merupakan generic function, dan mengembalikan List<T> data hasil dari semua Deffered nya
    dikarenakan mengembalikan berupa list<T> jadi tipe data dari semua deferred nya harus sama semua
     */
    @Test
    fun testAwaitAll(){
        runBlocking {
            val time = measureTimeMillis {
                val foo1: Deferred<Int> = GlobalScope.async { getFoo() }
                val bar1: Deferred<Int> = GlobalScope.async { getBar() }
                val foo2: Deferred<Int> = GlobalScope.async { getFoo() }
                val bar2: Deferred<Int> = GlobalScope.async { getBar() }

                val result = awaitAll(foo1, foo2, bar1, bar2).sum() // dijalankan secara async jadi waktu yg dibutuhkan hanya 1detik
                println("Result : $result") // output, "Result : 40"
            }
            println("Total time : $time") // output, "Total time : 1081"
        }
    }

}