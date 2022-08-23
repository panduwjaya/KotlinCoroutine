package programmer.zaman.now.coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.jupiter.api.Test
import java.util.*

class FlowTest {
    /*
    ======= Asynchronous Flow ========

    * Asynchronous Flow
    Sampai saat ini kita hanya membahas tentang coroutine yang tidak mengembalikan value (launch) dan mengembalikan satu value (async)
    bagaimana jika kita butuh sebuah coroutine yang mengembalikan data berkali-kali seperti layaknya collection? Kotlin mendukung hal tersebut dengan nama Flow.
    Flow mirip dengan sequence di Kotlin Collection, yang membedakan adalah flow berjalan sebagai coroutine dan kita bisa menggunakan suspend function di flow.
    Flow adalah collection cold atau lazy, artinya jika tidak diminta datanya, flow tidak akan berjalan (kode nya tidak akan dieksekusi)
    Dikarenakan flow adalah lazy maka diperlukan sebuah kode untuk memanggilnya agar mau berjalan yaitu menggunakan collect()

    * Membuat Flow
    -Untuk membuat flow, kita bisa menggunakan function flow()
    -Di dalam flow untuk mengirim data ke flow kita bisa menggunakan function emit()
     fungsi dari emit adalah untuk mengirim ke collect(),saat emit mendapat data maka secara langsung akan diterima di collect()
    -Untuk mengakses data yang ada di flow, kita bisa menggunakan function collect()
     Dikarenakan flow adalah lazy maka diperlukan sebuah kode untuk memanggilnya agar mau berjalan yaitu menggunakan collect(),apabila tidak dipanggil maka kode tsb tidak akan dieksekusi
     */
    @Test
    fun testFlow() {
        val flow1: Flow<Int> = flow {
            println("Start flow")
            repeat(5) {
                println("Emit $it")
                emit(it)
            }
        }

        runBlocking {
            // dikarenakan collect merupakan suspend function maka untuk memanggilnya perlu didalam coroutine yaitu menggunakan runBlocking
            // jika flow tidak dipanggil menggunakan collect maka kode tersebut tidak akan di eksekusi,karena sifatnya yg lazy
            flow1.collect {
                println("Receive $it")
            }
        }
        /*
        output:
        Start flow
        Emit 0
        Receive 0
        Emit 1
        Receive 1
        Emit 2
        Receive 2
        Emit 3
        Receive 3
        Emit 4
        Receive 4
        Emit 5
        Receive 5

        catatan:
        Sistem kerja pada flow mirip dengan menggunakan squence,yaitu dikerjakan secara berurut
        Tidak menyelesaikan salah satu terlebih dahulu sampai selesai,namun dikerjakan secara bersama dan berurutan
        Saat emit() mendapat data maka secara langsung data tersebut diterima di collect()
        Namun jika collect tidak panggil maka data tersebut tidak akan dieksekusi karena collect() bersifat lazy
        Maka dari itu hasil dari output pada flowTest terlihat squence atau berurutan,karena setiap proses dikerjakan secara langsung tanpa saling tunggu
         */
    }

    /*
    ====== Flow Operator ======

    Flow Operator
    Flow mirip dengan Kotlin Collection, memiliki banyak operator
    Hampir semua operator yang ada di Kotlin Collection ada juga di Flow, seperti map, flatMap, filter, reduce, dan lain-lain
    Yang membedakan dengan operator yang ada di Kotlin Collection adalah, operator di Flow mendukung suspend function

    catatan:
    Kelebihan,kita tidak perlu kotlin collection apabila ingin membuat collection di coroutine
    Cukup menggunakan flow semua operartor yg ada seperti map, flatMap, filter, reduce, dan lain-lain didukung oleh flow operator
    Sehingga operator-operator tersebut dapat kita gunakan saat menggunkan flow tanpa tambahan kotlin collection
     */
    suspend fun numberFlow(): Flow<Int> = flow {
        repeat(11) {
            emit(it)
        }
    }

    suspend fun changeToString(number: Int): String {
        delay(1000)
        return "Number $number"
    }

    @Test
    fun testFlowOperator() {
        runBlocking {
            val flow1 = numberFlow()
            // disini kita akan menggunakan operator filter untuk memfilter bilangan genap
            // kita juga dapat menggunakan macam-macam operator menggunakan (ctrl + spasi)
            flow1.filter { it % 2 == 0 }
                    .map { changeToString(it) }
                    .collect { println(it) }
        }
        /*
        output:
        Number 0
        Number 2
        Number 4
        Number 6
        Number 8
        Number 10

        catatan:
        Menampilkan output value hasil filter bilangan genap
         */
    }

    /*
    ========= Flow Exception ========

    Flow Exception
    Saat terjadi exception pada flow, di bagian operator apapun, maka flow akan berhenti, lalu exception akan di throw oleh flow
    Untuk menangkap exception tersebut, kita bisa menggunakan block try-catch
    Namun flow juga menyediakan operator untuk menangkap exception tersebut, nama functionnya adalah catch()
    Dan untuk finally, flow juga sudah menyediakan operatornya, nama function nya adalah onCompletion()
    Ingat, jika terjadi error di flow, flow akan dihentikan
    Jika kita ingin flow tidak berhenti saat terjadi error,Pastikan kita selalu melakukan try catch di kode flow nya

    fungsi:
    - fungsi dari map adalah untuk memvalidasi
    - fungsi dari onEach adalah untuk menangkap setiap value yg ada
    - fungsi dari catch adalah untuk menangkap exception yg ada
    - onCompletion berfungsi untuk menentukan apa yg akan dikerjakan setelah program selesai dieksekusi
      baik hasil eksekusi itu gagal atau berhasil output dari onCompletion akan tetap muncul
    - collect untuk memanggil data pada flow
     */
    @Test
    fun testFlowException() {
        runBlocking {
            numberFlow()
                    .map { check(it < 10); it }
                    .onEach { println(it) }
                    .catch { println("Error ${it.message}") }
                    .onCompletion { println("Done") }
                    .collect()
        }
        /*
        output:
        0
        1
        2
        3
        4
        5
        6
        7
        8
        9
        Error Check failed.
        Done

        catatan:
        Ketika tidak sesuai dengan validasi maka akan terjadi error,error tersebut akan ditangkap menggunakan catch()
         */
    }

    /*
    ======= Cancellable Flow =======

    Membatalkan Flow
    Flow adalah coroutine, artinya dia bisa dibatalkan
    Untuk membatalkan flow, caranya sangat mudah, kita bisa menggunakan function cancel() milik coroutine scope
    function cancel() tersebut akan secara otomatis membatalkan job coroutine
     */
    @Test
    fun testCancellableFlow() {
        val scope = CoroutineScope(Dispatchers.IO)
        runBlocking {
            val job = scope.launch {
                numberFlow()
                        .onEach {
                            if (it > 9) cancel() // disini kita akan membatalkan flow nya
                            else println(it)
                        }
                        .collect()
            }
            job.join()
        }
        /*
        ouput:
        0
        1
        2
        3
        4
        5
        6
        7
        8
        9

        catatan:
        Saat mencapai nilai 10 maka perulangan flow tersebut akan divalidasi dan job coroutine akan dibatalkan
         */
    }

    @Test
    fun testSharedFlow() {
        val scope = CoroutineScope(Dispatchers.IO)
        val sharedFlow = MutableSharedFlow<Int>()

        scope.launch {
            repeat(10) {
                println("   Send     1 : $it : ${Date()}")
                sharedFlow.emit(it)
                delay(1000)
            }
        }

        scope.launch {
            sharedFlow.asSharedFlow()
                    .buffer(10)
                    .map { "Receive Job 1 : $it : ${Date()}" }
                    .collect {
                        delay(1000)
                        println(it)
                    }
        }

        scope.launch {
            sharedFlow.asSharedFlow()
                    .buffer(10)
                    .map { "Receive Job 2 : $it : ${Date()}" }
                    .collect {
                        delay(2000)
                        println(it)
                    }
        }

        runBlocking {
            delay(22_000)
            scope.cancel()
        }
    }

    @Test
    fun testStateFlow() {
        val scope = CoroutineScope(Dispatchers.IO)
        val stateFlow = MutableStateFlow(0)

        scope.launch {
            repeat(10) {
                println("   Send     1 : $it : ${Date()}")
                stateFlow.emit(it)
                delay(1000)
            }
        }

        scope.launch {
            stateFlow.asStateFlow()
                    .map { "Receive Job 2 : $it : ${Date()}" }
                    .collect {
                        println(it)
                        delay(5000)
                    }
        }

        runBlocking {
            delay(22_000)
            scope.cancel()
        }
    }
}