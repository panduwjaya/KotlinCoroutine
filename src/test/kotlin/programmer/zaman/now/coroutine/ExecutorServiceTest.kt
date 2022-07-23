package programmer.zaman.now.coroutine

import org.junit.jupiter.api.Test
import java.util.*
import java.util.concurrent.Executors

/*
Executor Service
bagian Thread pada program termasuk kedalam kategori mahal setelah bagian program, karena sizenya lumayan yakni 512kb-1MB

Masalah Dengan Thread
Thread adalah object yang lumayan berat, sekitar 512kb - 1MB, sehingga jika terlalu banyak membuat Thread, penggunaan memory di aplikasi kita akan membengkak
program 32bit = 512kb/thread dan program 64bit = 1MB/thread
Sehingga penggunaan Thread secara manual sangat tidak disarankan
Thread sendiri sebenernya bisa digunakan ulang jika proses sudah selesai dilakukan
namun membuat thread agar dapat digunakan secara berulang ulang secara manual itu sangatlah sulid
maka dari itu diperlukan ExecutorService agar thread dapat digunakan secara berulang ulang tidak secara manual

ExecutorService
ExecutorService adalah fitur di JVM yang bisa digunakan untuk manajemen Thread.
Penggunakan ExecutorService lebih direkomendasikan dibandingkan menggunakan Thread secara manual
ExecutorService secara simplenya:
-adalah object untuk me manage thread
-jadi kita bisa memasukan berbagai macam task kedalam ExecutorService agar dapat me-manage berapa banyak thread yg digunakan untuk menjalankan task
-sehingga kita tidak perlu secara manual me-manage berapa banyak thread yg akan digunakan untuk menjalankan task
ExecutorService adalah sebuah interface, untuk membuat objectnya, kita bisa menggunakan class Executors, terdapat banyak helper method di class Executors.
di dalam class Executor terdapat object Method(untuk kotlin) atau static Method(untuk java)

Executors Method
-newSingleThreadExecutor = Membuat ExecutorService dengan didalamnya terdapat 1 thread
-newFixedThreadPool(int) = Membuat ExecutorService dengan didalamnya terdapat n thread
-newCachedThreadPool() = Membuat ExecutorService dengan jumlah thread yg akan meningkat sesuai kebutuhan
-> artinya ketika banyak tugas yg masuk maka jumlah thread yg digunakan akan meningkat dan ketika tidak terdapat tugas maka jumlah thread yg digunakan akan menurun
-> pada newCachedThreadPool() tidak terdapat thread yg digunakan,maka dampak buruknya apabila terjadi over thread maka akan terjadi error memory/outOfMemory

Threadpool
Implementasi ExecutorService yang terdapat di class Executors adalah class ThreadPoolExecutor
Di dalam ThreadPool, terdapat data queue (antrian) tempat menyimpan semua proses sebelum di eksekusi oleh Thread yang tersedia di ThreadPool
jadi runnable yg belum di eksekusi akan diletakan pada queue(antrian) untuk menunggu proses eksekusi
Hal ini jadi kita bisa mengeksekusi sebanyak-banyaknya Runnable walaupun Thread tidak cukup untuk mengeksekusi semua Runnable
Runnable yang tidak dieksekusi akan menunggu di queue sampai Thread sudah selesai mengeksekusi Runnable yang lain/runnable yg belum diesksekusi
 */

class ExecutorServiceTest {

//    penggunaan newSingleThreadExecutor
    @Test
    fun testSingleThreadPool(){
        val executorService = Executors.newSingleThreadExecutor()
        repeat(10){
            val runnable = Runnable {
                Thread.sleep(1000)
                println("Done $it ${Thread.currentThread().name} ${Date()}")
            }
            executorService.execute(runnable)
            println("Selesai memasukkan runnable $it")
        }

        println("MENUNGGU")
        Thread.sleep(11_000)
        println("SELESAI")
    }

//    penggunaan newFixedThreadPool
//    repeat berarti berapa banyak task yg sama tersebut diulang pada thread
    @Test
    fun testFixThreadPool(){
        val executorService = Executors.newFixedThreadPool(3)
        repeat(10){
            val runnable = Runnable {
                Thread.sleep(1000)
                println("Done $it ${Thread.currentThread().name} ${Date()}")
            }
            executorService.execute(runnable)
            println("Selesai memasukkan runnable $it")
        }

        println("MENUNGGU")
        Thread.sleep(11_000)
        println("SELESAI")
    }

//    penggunaan newCachedThreadPool
//    namun newCachedThreadPool tidak disarankan,karena ketika task meningkat maka penggunaan thread meningkat secara otomatis
//    yang mana apabila terdapat over thread akan mengakibatkan outOfMemory,yg mana dapat memberatkan hardware
    @Test
    fun testCacheThreadPool(){
        val executorService = Executors.newCachedThreadPool()
        repeat(100){
            val runnable = Runnable {
                Thread.sleep(1000)
                println("Done $it ${Thread.currentThread().name} ${Date()}")
            }
            executorService.execute(runnable)
            println("Selesai memasukkan runnable $it")
        }

        println("MENUNGGU")
        Thread.sleep(11_000)
        println("SELESAI")
    }

}