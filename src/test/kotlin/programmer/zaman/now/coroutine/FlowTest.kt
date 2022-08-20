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
            // dikarenakan collect merupakan suspend function maka untuk memanggilnya perlu didalam runBlocking
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

    suspend fun numberFlow(): Flow<Int> = flow {
        repeat(100) {
            emit(it)
        }
    }

    suspend fun changeToString(number: Int): String {
        delay(100)
        return "Number $number"
    }

    @Test
    fun testFlowOperator() {
        runBlocking {
            val flow1 = numberFlow()
            flow1.filter { it % 2 == 0 }
                    .map { changeToString(it) }
                    .collect { println(it) }
        }
    }

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
    }

    @Test
    fun testCancellableFlow() {
        val scope = CoroutineScope(Dispatchers.IO)
        runBlocking {
            val job = scope.launch {
                numberFlow()
                        .onEach {
                            if (it > 10) cancel()
                            else println(it)
                        }
                        .collect()
            }
            job.join()
        }
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