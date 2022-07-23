package programmer.zaman.now.coroutine

import org.junit.jupiter.api.Test
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.system.measureTimeMillis

/*
Callable
Sebelumnya kita sudah tau, bahwa Thread akan mengeksekusi isi method run yang ada di interface Runnable,
hanya saja masalahnya, return value dari Runnable adalah void (unit), artinya tidak mengembalikan data
Jika kita ingin mengeksekusi sebuah kode yang mengembalikan data,
kita bisa menggunakan interface Callable, dimana terdapat method call dan return value nya adalah generic
Kita bisa menggunakan ExecutorService.submit(callable) untuk mengeksekusi Callable, dan hasilnya adalah sebuah Future<T>

Future
Future merupakan return value untuk eksekusi Callable
Dengan Future, kita bisa mengecek status apakah proses telah selesai, atau bisa mendapatkan data hasil return callable,
atau bahkan membatalkan proses callable yang sedang berjalan
artinya kita baru bisa mendapatkan return value ketika prosesnya selesai,jadi tidak ada return value selama proses belum selesai
*/

class FutureTest {

    val executorService = Executors.newFixedThreadPool(10)

    fun getFoo(): Int {
        Thread.sleep(5000)
        return 10
    }

    fun getFoo1(): Int {
        Thread.sleep(5000)
        return 10
    }

    fun getBar(): Int {
        Thread.sleep(5000)
        return 10
    }

    fun getBar1(): Int {
        Thread.sleep(5000)
        return 10
    }

//    test dibawah ini menjalankan thread secara nonParallel
    @Test
    fun testNonParallel(){
        val time = measureTimeMillis {
            val foo = getFoo()
            val bar = getBar()
            val foo1 = getFoo1()
            val bar1 = getBar1()
            val result = foo + bar + foo1 + bar1
            println("Total : $result") //output, Total : 40
        }
        println("Total time : $time") //output, Total time : 20027 atau 20detik
//    cara kerja non paralel adalah,setiap task akan dieksekusi setelah task lain selesai dieksekusi
//    jadi waktu yg dibutuhkan adalah 20detik karena setiap task dieksekusi secara satu per satu,yg mana terdapat 4 task (4 x 5 detik = 20detik)
    }

//    test dibawah ini menjalankan thread secara paralel
//    measureTimeMillis berfungsi menghitung estimasi waktu yg dibutuhkan dalam menjalankan task
//    didalam future terdapat banyak method yg dapat kita manfaatkan,untuk dapat melihat method apa saja (ctrl+klik)
    @Test
     fun testFuture() {
        val time = measureTimeMillis {
            val foo: Future<Int> = executorService.submit(Callable { getFoo() })
            val bar: Future<Int> = executorService.submit(Callable { getBar() })
            val foo1: Future<Int> = executorService.submit(Callable { getFoo1() })
            val bar1: Future<Int> = executorService.submit(Callable { getBar1() })

            val result = foo.get() + bar.get() + foo1.get() + bar1.get()
            println("Total : $result") //output, Total : 40
        }
        println("Total time : $time") //output, Total time : 5033
//    alasan waktu yg dibutuhkan tes secara paralel lebih cepat karena setiap task lgsg dieksekusi tanpa harus menunggu task lain selesai
//    jadi dalam waktu tunggu 5 detik seluruh task sudah selesai karena dieksekusi secara bersamaan
    }
}