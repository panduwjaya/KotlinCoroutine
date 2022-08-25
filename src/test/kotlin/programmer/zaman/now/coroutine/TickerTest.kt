package programmer.zaman.now.coroutine

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.util.*

class TickerTest {
    /*
    ========= ticker Function =========

    ticker Function
    ticker adalah function yang bisa kita gunakan untuk membuat channel mirip dengan timer
    Dengan ticker, kita bisa menentukan sebuah pesan akan dikirim dalam waktu timer yang sudah kita tentukan
    Ini cocok jika kita ingin membuat timer menggunakan coroutine dan channel
    Return value dari ticker function adalah ReceiveChannel<Unit>, dan setiap kita receive data, datanya hanya berupa data null
    fungsi dari ticker function itu sendiri hanya menjadi sebuah trigger karena return dari ticker function itu sendiri unit atau tidak ada data
     */

    // contoh timer otomatis menggunakan ticker
    @Test
    fun testTicker() {
        val receiveChannel = ticker(delayMillis = 1000)
        runBlocking {
            val job = launch {
                repeat(10) {
                    receiveChannel.receive()
                    println(Date())
                }
            }
            job.join()
        }
        /*
        output:
        Fri Aug 26 00:25:03 ICT 2022
        Fri Aug 26 00:25:04 ICT 2022
        Fri Aug 26 00:25:05 ICT 2022
        Fri Aug 26 00:25:06 ICT 2022
        Fri Aug 26 00:25:07 ICT 2022
        Fri Aug 26 00:25:08 ICT 2022
        Fri Aug 26 00:25:09 ICT 2022
        Fri Aug 26 00:25:10 ICT 2022
        Fri Aug 26 00:25:11 ICT 2022
        Fri Aug 26 00:25:12 ICT 2022

        Hasil output yg ditampilkan oleh timer manual(globalScope dan while) dan otomatis(ticker)sama
         */
    }

    // contoh timer manual menggunakan while dan globalScope
    @Test
    fun testTimer() {
        val receiveChannel = GlobalScope.produce<String?> {
            while (true) {
                delay(1000)
                send(null)
            }
        }
        runBlocking {
            val job = launch {
                repeat(10) {
                    receiveChannel.receive()
                    println(Date())
                }
            }
            job.join()
        }
        /*
        output:
        Fri Aug 26 00:25:03 ICT 2022
        Fri Aug 26 00:25:04 ICT 2022
        Fri Aug 26 00:25:05 ICT 2022
        Fri Aug 26 00:25:06 ICT 2022
        Fri Aug 26 00:25:07 ICT 2022
        Fri Aug 26 00:25:08 ICT 2022
        Fri Aug 26 00:25:09 ICT 2022
        Fri Aug 26 00:25:10 ICT 2022
        Fri Aug 26 00:25:11 ICT 2022
        Fri Aug 26 00:25:12 ICT 2022

        Hasil output yg ditampilkan oleh timer manual(globalScope dan while) dan otomatis(ticker)sama
         */
    }
}