package programmer.zaman.now.coroutine

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.*

/*
=============== PENGENALAN COROUTINE ==================

Problem di Java Thread
Java Thread tidak didesain untuk melakukan Concurrency Programming.
Java Thread hanya bisa melakukan satu hal sampai selesai, baru melakukan hal lain
Artinya satu thread hanya bisa digunakan pada satu task/pekerjaan saja
Salah satu implementasi Concurrency Programming yang saat ini paling populer adalah Go-Lang Goroutine
Untungnya di Kotlin, ada fitur yang bernama Coroutine, salah satu implementasi Concurrency Programming
karena target pasar kotlin adalah mobile yang tidak cocok apabila menggunakan sistem parallel programming
karena sifat processor pada mobile yg rendah core tidak bisa menjalankan banyak thread sekaligus dalam jumlah banyak

Pengenalan Coroutine
Coroutine sering diistilahkan sebagai lightweight thread (thread ringan), walaupun sebenernya coroutine sendiri bukanlah thread.
Coroutine sebenarnya di eksekusi di dalam thread, namun dengan coroutine sebuah thread bisa memiliki kemampuan untuk menjalankan beberapa coroutine secara bergantian (concurrent)
Artinya jika sebuah thread menjalankan 10 coroutine, sebenarnya thread akan menjalankan coroutine satu per satu secara bergantian
Perbedaan lain thread dan coroutine adalah coroutine itu murah dan cepat, sehingga kita bisa membuat ribuan atau bahkan jutaan coroutine secara cepat dan murah tanpa takut kelebihan memory

Suspend Function
Suspend computation adalah komputasi yang bisa ditangguhkan (ditunda waktu eksekusinya).
Sebelumnya kita tahu untuk menangguhkan komputasi di Java, kita biasanya menggunakan Thread.sleep(), sayangnya Thread.sleep() akan mem-block thread yang sedang berjalan saat ini. Sehingga tidak bisa digunakan.
Kotlin memiliki sebuah fitur bernama suspending function, dimana kita bisa menangguhkan waktu eksekusi sebuah function
tanpa harus mem-block thread yang sedang menjalankannya.
Jadi dengan suspend function kita bisa menangguhkan pekerjaan suatu task,lalu mengerjakan task yg lainnya,kemudian kembali ke pekerjaan sebelumnya
Syarat menjalankan suspend function di Kotlin adalah, harus dipanggil dari suspend function lainnya atau di dalam coroutine.
namun secara default coroutine merupakan suspend function jadi kita tidak perlu mengubah banyak method lain untuk memanggil suspend function
*/

class SuspendFunctionTest {

    /*
    ketika kita memanggil suspend function haruslah kita memanggilnya di dalam suspend function juga
    ketika kita menggunakan delay kita wajib memanggilnya di dalam suspend function
    saat kita menjalankan delay tanpa suspend function maka akan terjadi error
    Dan untuk memanggil suspend function tersebut kita harus menggunakan runBlocking
     */
    suspend fun helloWorld(){
        println("Hello : ${Date()} : ${Thread.currentThread().name}")
        delay(2_000)
        println("World : ${Date()} : ${Thread.currentThread().name}")
    }

    /*
    Dan untuk memanggil suspend function tersebut kita harus menggunakan runBlocking sebagai pengganti suspend function

    **memanggil suspend function menggunakan function biasa:

    @Test
    fun testSuspend(){
        helloWorld()
    }

    -> function testSuspend() diatas akan error,karena yg boleh memanggil suspend function hanya suspen function itu sendiri atau menggunakan runBlocking

    runBlocking merupakan perintah untuk menjalankan sebuah suspend functin,tanpa runBlocking maka suspend function tidak bisa dijalankan
    namun runBlocking hanya boleh digunakan untuk melakukan test saja dan tidak boleh digunakan pada pekerjaan

    cara kerja runBlocking:
    - runBlocking menjalankan sebuah coroutine
    - kemudian runBlocking mem-Block sebuah thread dan me-running suspend function didalam lambda runBlocking
    - hingga suspend function yg kita panggil selesai dijalankan
    */

    @Test
    fun testSuspendFunction(){
        runBlocking {
            helloWorld() //memanggil suspend function helloWorld()
            // ouput Hello : Sat Jul 23 11:24:25 ICT 2022 : Test worker @coroutine#1
            // ouput World : Sat Jul 23 11:24:27 ICT 2022 : Test worker @coroutine#1
            // artinya semua coroutine dijalankan secara concurency dalam satu thread yg sama yaitu "Test worker @coroutine#1"
        }
    }

}