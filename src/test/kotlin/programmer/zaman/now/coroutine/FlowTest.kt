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

    /*
    ========= Shared Flow ==========

    Shared Flow vs Flow
    Shared Flow adalah turunan dari Flow, sehingga apa yang bisa dilakukan di Flow, bisa juga dilakukan di Shared Flow
    Kemampuan Shared Flow yang tidak dimiliki oleh Flow adalah, pada Shared Flow, kita bisa membuat lebih dari satu receiver
    Pada flow hanya satu receiver sedangkan pada shared flow kita bisa membuat lebih dari satu receiver
    Artinya ketika dipanggil menggunakan collect hanya flow tersebut yg dapat menerima datanya
    Selain itu Shared Flow bersifat aktif atau hot, yang artinya ketika kita mengirim data ke Shared Flow,
    Data langsung dikirim ke receiver tanpa perlu di collect terlebih dahulu oleh si receiver

    Shared Flow vs Broadcast Channel
    Shared Flow mulai dikenalkan di Kotlin 1.4
    Shared Flow dirancang sebagai pengganti Broadcast Channel,pada kotlin 1.4 keatas broadcast sudah tidak direkomendasikan dan digantikan oleh shared flow
    Shared Flow adalah turunan dari Flow, sehingga mendukung semua Flow operator (map, flatMap, filter, reduce, dan lain-lain)
    Hal ini yang sangat membedakan dengan Channel yang hanya bisa menggunakan receive() untuk menerima data,di Shared Flow, kita bisa melakukan operasi apapun bawaan dari Flow operator seperti map,filter dan lain-lain
    Shared Flow mendukung configurable buffer overflow strategy karena bisa menggunakan Flow Operator
    Shared Flow bukanlah channel, walaupun shared flow pengganti broadcast channel namun shared flow bukanlah channel sehingga tidak ada operasi close
    Untuk membuat receiver dari Shared Flow, kita bisa menggunakan function asSharedFlow()

    kelebihan:
    kelebihan penggunaan shareFlow dibanding menggunakan broadCast channel adalah lebih fleksibel
    Serta bisa menggunakan berbagai macam fitur yang tersedia

     */

    @Test
    fun testSharedFlow() {
        val scope = CoroutineScope(Dispatchers.IO)
        val sharedFlow = MutableSharedFlow<Int>()

        scope.launch {
            repeat(10) {
                println("   Send     1 : $it : ${Date()}")
                // membuat sender menggunakan emit
                sharedFlow.emit(it)
                delay(1000)
            }
        }

        // pada sharedFlow kita dapat membuat receiver lebih dari satu buah
        scope.launch {
            // membuat receiver data pertama menggunakan asSharedFlow
            sharedFlow.asSharedFlow()
                    // kita bisa menggunakan buffer pada flow
                    .buffer(10) // this is buffer
                    .map { "Receive Job 1 : $it : ${Date()}" }
                    .collect {
                        delay(1000)
                        println(it)
                    }
        }

        scope.launch {
            // membuat receiver data kedua menggunakan asSharedFlow
            sharedFlow.asSharedFlow()
                    // kita bisa menggunakan buffer pada flow
                    // fungsi buffer adalah ketika mengirim data ke shareFlow dan belum diterima akan ditampung di dalam sharedflow
                    .buffer(10) // this is buffer
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
        /*
        output:
           Send     1 : 0 : Sat Aug 27 09:55:06 ICT 2022
           Send     1 : 1 : Sat Aug 27 09:55:07 ICT 2022
           Send     1 : 2 : Sat Aug 27 09:55:08 ICT 2022
        Receive Job 1 : 1 : Sat Aug 27 09:55:07 ICT 2022
           Send     1 : 3 : Sat Aug 27 09:55:09 ICT 2022
        Receive Job 2 : 1 : Sat Aug 27 09:55:07 ICT 2022
        Receive Job 1 : 2 : Sat Aug 27 09:55:08 ICT 2022
           Send     1 : 4 : Sat Aug 27 09:55:10 ICT 2022
        Receive Job 1 : 3 : Sat Aug 27 09:55:09 ICT 2022
        Receive Job 2 : 2 : Sat Aug 27 09:55:09 ICT 2022
           Send     1 : 5 : Sat Aug 27 09:55:11 ICT 2022
        Receive Job 1 : 4 : Sat Aug 27 09:55:10 ICT 2022
           Send     1 : 6 : Sat Aug 27 09:55:12 ICT 2022
        Receive Job 1 : 5 : Sat Aug 27 09:55:11 ICT 2022
        Receive Job 2 : 3 : Sat Aug 27 09:55:11 ICT 2022
           Send     1 : 7 : Sat Aug 27 09:55:13 ICT 2022
        Receive Job 1 : 6 : Sat Aug 27 09:55:12 ICT 2022
           Send     1 : 8 : Sat Aug 27 09:55:14 ICT 2022
        Receive Job 1 : 7 : Sat Aug 27 09:55:13 ICT 2022
        Receive Job 2 : 4 : Sat Aug 27 09:55:13 ICT 2022
           Send     1 : 9 : Sat Aug 27 09:55:15 ICT 2022
        Receive Job 1 : 8 : Sat Aug 27 09:55:14 ICT 2022
        Receive Job 1 : 9 : Sat Aug 27 09:55:15 ICT 2022
        Receive Job 2 : 5 : Sat Aug 27 09:55:15 ICT 2022
        Receive Job 2 : 6 : Sat Aug 27 09:55:17 ICT 2022
        Receive Job 2 : 7 : Sat Aug 27 09:55:19 ICT 2022
        Receive Job 2 : 8 : Sat Aug 27 09:55:21 ICT 2022
        Receive Job 2 : 9 : Sat Aug 27 09:55:23 ICT 2022
         */
    }

    /*
    ========= State Flow =========

    State Flow
    State Flow adalah turunan dari Shared Flow, artinya di State Flow, kita bisa membuat banyak receiver Flow
    Hal yg bisa kita lakukan di flow dan shared flow juga dapat kita lakukan pada state flow
    Pada shared flow receiver akan menerima semua data yg ada sedangkan State Flow, receiver hanya akan menerima data paling baru
    Jadi jika ada receiver yang sangat lambat dan sender mengirim data terlalu cepat, yang akan diterima oleh receiver adalah data paling akhir/data terbaru
    State Flow cocok digunakan untuk maintain state, dimana memang biasanya state itu biasanya hanya satu data, tidak peduli berapa kali perubahan data tersebut,yang paling penting pada state adalah data terakhir
    Untuk mendapatkan data state nya, kita bisa menggunakan field value di State Flow
    Untuk membuat receiver kita bisa menggunakan asStateFlow()
    State Flow bisa dirancang sebagai pengganti Conflated Broadcast Channel

    catatan:
    Sebenarnya pada state flow hanya terdapat satu buah data saja,sebab jika terdapat 2 data maka diambil hanya terbaru saja
    Sedangkan data yang sebelumnya telah ada akan direplace oleh data terbaru jadi sebenarnya state flow hanya menyimpan satu data saja yaitu data terbaru

    fungsi:
    State flow dirancang sebagai pengganti conflated broadcast channel karena dinilai lebih efiesien dan mempunya fungsi yg sama yaitu sama-sama hanya mengirim data terbaru saja
     */
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

        // pada state flow tidak diperbolehkan menggunakan buffer dikarenakan apabila menggunakan buffer data yg lama akan tetap tersimpan didalam antrian
        // sedangkan pada stateFlow bertujuan me-replace data lama dengan data baru dan hanya mengirim data terbaru saja
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
        /*
        output:
           Send     1 : 0 : Sat Aug 27 21:26:08 ICT 2022
        Receive Job 2 : 0 : Sat Aug 27 21:26:08 ICT 2022
           Send     1 : 1 : Sat Aug 27 21:26:09 ICT 2022
           Send     1 : 2 : Sat Aug 27 21:26:10 ICT 2022
           Send     1 : 3 : Sat Aug 27 21:26:11 ICT 2022
           Send     1 : 4 : Sat Aug 27 21:26:12 ICT 2022
        Receive Job 2 : 4 : Sat Aug 27 21:26:13 ICT 2022
           Send     1 : 5 : Sat Aug 27 21:26:13 ICT 2022
           Send     1 : 6 : Sat Aug 27 21:26:14 ICT 2022
           Send     1 : 7 : Sat Aug 27 21:26:15 ICT 2022
           Send     1 : 8 : Sat Aug 27 21:26:16 ICT 2022
           Send     1 : 9 : Sat Aug 27 21:26:17 ICT 2022
        Receive Job 2 : 9 : Sat Aug 27 21:26:18 ICT 2022

        Berdasarkan output terlihat jelas bahwa data yg diterima oleh receive selalu data terbaru,berarti sesuai dengan fungsi dari stateFlow
        fungsi dari stateFlow adalah me-replace data lama dengan data terbaru dan selalu mengirimkan data yg terbaru ke receiver
         */
    }
}