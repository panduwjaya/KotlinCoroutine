package programmer.zaman.now.coroutine

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test

class ExceptionHandlingTest {

    /*
    ========== Exception Handling ==========

    Exception Propagation
    Secara garis besar, exception di coroutine itu ada yang di ekspose ke yang memanggil coroutine ada yang tidak.
    Pada launch, exception tidak akan di ekspose ketika memanggil function join, namun pada async exception akan di expose ketika memanggil function await
     */

    // kode launch
    @Test
    fun testExceptionLaunch() {
        runBlocking {
            val job = GlobalScope.launch {
                println("Start coroutine")
                throw IllegalArgumentException()
            }

            job.join()
            println("Finish")
        }
    }
    /*
    output kode launch:
    Start coroutine
    Exception in thread "DefaultDispatcher-worker-2 @coroutine#2" java.lang.IllegalArgumentException
        at programmer.zaman.now.coroutine.ExceptionHandlingTest$testExceptionLaunch$1$job$1.invokeSuspend(ExceptionHandlingTest.kt:22)
        at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
        at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:106)
        at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:571)
        at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:738)
        at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:678)
        at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:665)
    Finish

    catatan:
    perbedaan pada kode launch dan kode async tanpa try catch adalah pada kode launch job tetap dieksekusi walaupun terjadi error
    terlihat pada output "finish" yg menandakan bahwa job tetap dieksekusi tanpa ada masalah walaupun terdapat error sebelum bagian job di eksekusi
     */

    // kode Async tanpa try,catch dan finally
    @Test
    fun testExceptionAsyncNoTryCatch(){
        runBlocking{
            val deferred=GlobalScope.async{
                println("Start coroutine")
                throw IllegalArgumentException()
            }
            deferred.await()
            println("Finish")
        }
    }
    /*
    output kode Async tanpa try,catch dan finally:
    Start coroutine

    java.lang.IllegalArgumentException
        at programmer.zaman.now.coroutine.ExceptionHandlingTest$testExceptionAsyncNoTryCatch$1$deferred$1.invokeSuspend(ExceptionHandlingTest.kt:53)
        (Coroutine boundary)
        at kotlinx.coroutines.DeferredCoroutine.await$suspendImpl(Builders.common.kt:101)
        at programmer.zaman.now.coroutine.ExceptionHandlingTest$testExceptionAsyncNoTryCatch$1.invokeSuspend(ExceptionHandlingTest.kt:55)
    Caused by: java.lang.IllegalArgumentException
        at programmer.zaman.now.coroutine.ExceptionHandlingTest$testExceptionAsyncNoTryCatch$1$deferred$1.invokeSuspend(ExceptionHandlingTest.kt:53)
        at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
        at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:106)
        at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:571)
        at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:738)
        at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:678)
        at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:665)

    catatan:
    perbedaan pada kode launch dan kode async tanpa try catch adalah pada kode launch job tetap dieksekusi walaupun terjadi error
    namun pada kode async tanpa try dan catch tidak muncul output "finish" yg mana source code tersebut terdapat setelah exception error
    hal tersebut yg membuat perbedaan antara kode launch dan kode async tanpa try dan catch
     */


    // kode Async dengan try,catch dan finally
    @Test
    fun testExceptionAsync() {
        runBlocking {
            val deferred = GlobalScope.async {
                println("Start coroutine")
                throw IllegalArgumentException()
            }

            try {
                deferred.await()
            } catch (error: IllegalArgumentException) {
                println("Error")
            } finally {
                println("Finish")
            }
        }
    }
    /*
    output kode Async dengan try,catch dan finally:
    Start coroutine
    Error
    Finish

    catatan:
    untuk menghindari exception error sehingga tidak dapat memunculkan output,maka pada Async diperlukan tambahan Try dan Catch
    Tujuan nya adalah untuk dapat menghandle exception yg ada pada kode async
    Sehingga dapat menampilkan output dari source code setelah source code yg menimbulkan Exception
     */

    /*
    Coroutine Exception Handler

    Coroutine Exception Handler
    Kadang kita ingin mengatur cara penangkapan exception di coroutine, hal ini bisa dilakukan dengan menggunakan interface CoroutineExceptionHandler
    CoroutineExceptionHandler adalah turunan dari CoroutineContext.Element, sehingga kita bisa menambahkannya kedalam coroutine context
    Ingat jenis CancellationException (dan turunannya) tidak akan diteruskan ke exception handler
    Coroutine exception handler hanya jalan di launch, tidak jalan di async dan deffered, untuk async dan deffered, kita tetap harus menangkap exception nya secara manual yatu menggunakan Try dan Catch

    catatan:
    Tujuan adanya Coroutine Exception handler adalah cara kita untuk meng handle exception tanpa menggunakan Try dan Catch
    Coroutine exception handler itu sendiri juga dapat kita masukan kedalam context di dalam coroutine karena masih termasuk turunan dari coroutine.context.element
     */

    @Test
    fun testExceptionHandler() {
        val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
            println("Ups error ${throwable.message}")
        }

        val scope = CoroutineScope(Dispatchers.IO + exceptionHandler)

        runBlocking {
            // dari globalScope
            val job1 = GlobalScope.launch(exceptionHandler) {
                println("Start coroutine")
                throw IllegalArgumentException("error")
            }
            job1.join()
            println("Finish")

            // dari scope
            val job2 = scope.launch {
                println("Start coroutine")
                throw IllegalArgumentException("error")
            }
            job2.join()
            println("Finish")
        }
    }
    /*
    output testExceptionHandler:

    // output job1
    Start coroutine
    Ups error error
    Finish

    // output job2
    Start coroutine
    Ups error error
    Finish

    catatan:
    Pada intinya kita bisa menggunakan Exception handler baik pada global maupun pada scope biasa/scope yg kita buat sendiri
    Kita juga bisa memasukan Exception handler kedalam context pada coroutine
     */
}