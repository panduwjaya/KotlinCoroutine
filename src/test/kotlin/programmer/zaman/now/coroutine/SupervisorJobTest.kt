package programmer.zaman.now.coroutine

import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors

class SupervisorJobTest {
    /*
    ========= Supervisor Job ===========

    Job
    Secara default, saat kita membuat coroutine scope atau menjalankan coroutine, tipe coroutine tersebut adalah Job
    Dalam Job, saat terjadi error di salah satu coroutine, maka error tersebut akan di propagate ke parent nya
    Dan secara otomatis parent akan membatalkan semua coroutine

    Penjelasan:
    Untuk menyelesaikan permasalahan mengenai behavior dari job itu sendiri yaitu menggunakan supervisor job
    Sehingga jika terdapat error maka tidak di propagate/dikirim ke parent nya serta tidak menimbulkan error ke semua coroutine yg ada
     */
    @Test
    fun testJob() {
        val dispatcher = Executors.newFixedThreadPool(10).asCoroutineDispatcher()
        val scope = CoroutineScope(dispatcher + Job())

        val job1 = scope.launch {
            delay(2000)
            println("Job 1 Done")
        }

        val job2 = scope.launch {
            delay(1000)
            throw IllegalArgumentException("Job 2 failed")
        }

        runBlocking {
            joinAll(job1, job2)
        }
    }
    /*
    output job:
        Exception in thread "pool-1-thread-3 @coroutine#2" java.lang.IllegalArgumentException: Job 2 failed
        at programmer.zaman.now.coroutine.SupervisorJobTest$testJob$job2$1.invokeSuspend(SupervisorJobTest.kt:32)
        at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
        at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:106)

    catatan:

     */

    /*
    Supervisor Job

    SupervisorJob adalah tipe Job lainnya
    Supervisor job bisa menjadikan setiap coroutine memiliki kemampuan untuk error secara mandiri
    Hal ini berakibat jika ada coroutine error, parent tidak akan membatalkan seluruh coroutine yang lain
     */
    @Test
    fun testSupervisorJob() {
        val dispatcher = Executors.newFixedThreadPool(10).asCoroutineDispatcher()
        val scope = CoroutineScope(dispatcher + SupervisorJob())

        val job1 = scope.launch {
            delay(2000)
            println("Job 1 Done")
        }

        val job2 = scope.launch {
            delay(1000)
            throw IllegalArgumentException("Job 2 failed")
        }

        runBlocking {
            joinAll(job1, job2)
        }
    }
    /*
    output SupervisorJob:
    Exception in thread "pool-1-thread-3 @coroutine#2" java.lang.IllegalArgumentException: Job 2 failed
        at programmer.zaman.now.coroutine.SupervisorJobTest$testSupervisorJob$job2$1.invokeSuspend(SupervisorJobTest.kt:69)
        at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
        at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:106)
        at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)
        at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
        at java.base/java.lang.Thread.run(Thread.java:829)
    Job 1 Done

    catatan:
    Output pada supervisorJob masih menampilkan output yg berasal dari child coroutine yaitu "Job 1 done"
    Dikarenakan pada SupervisorJob jika terdapat error pada salah satu coroutine child nya tidak akan membatalkan coroutine yg lain,berbeda dengan job biasa
     */

    /*
    ========= supervisorScope Function ===========

    supervisorScope Function
    Kadang ada kondisi dimana kita tidak memiliki akses untuk mengubah sebuah coroutine scope
    Karena secara default coroutine scope sifatnya adalah Job bukan SupervisorJob
    maka kita bisa menggunakan supervisorScope function untuk membuat coroutineScope biasa merubah sifat coroutineChild nya menjadi SupervisorJob
     */

    // kode Non SupervisorScope Function
    @Test
    fun testNonSupervisorScopeFunction() {
        val dispatcher = Executors.newFixedThreadPool(10).asCoroutineDispatcher()
        val scope = CoroutineScope(dispatcher + Job())

        runBlocking {
            scope.launch {
                launch {
                    delay(2000)
                    println("Child 1 Done")
                }
                launch {
                    delay(1000)
                    throw IllegalArgumentException("Child 2 Error")
                }
            }

            delay(3000)
        }
        /*
        output:
        Exception in thread "pool-1-thread-5 @coroutine#3" java.lang.IllegalArgumentException: Child 2 Error
            at programmer.zaman.now.coroutine.SupervisorJobTest$testNonSupervisorScopeFunction$1$1$2.invokeSuspend(SupervisorJobTest.kt:115)
            at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
            at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:106)

        catatan:
        jika kita tidak menggunakan Supervisor Function maka secara default sifatnya adalah job
        Sifat dari job itu sendiri adalah Saat terjadi error di salah satu coroutine, maka error tersebut akan di propagate ke parent nya
        secara otomatis parent akan membatalkan semua coroutine child nya
         */
    }

    // kode SupervisorScope Function
    @Test
    fun testSupervisorScopeFunction() {
        val dispatcher = Executors.newFixedThreadPool(10).asCoroutineDispatcher()
        val scope = CoroutineScope(dispatcher + Job())

        runBlocking {
            scope.launch {
                supervisorScope {
                    launch {
                        delay(2000)
                        println("Child 1 Done")
                    }
                    launch {
                        delay(1000)
                        throw IllegalArgumentException("Child 2 Error")
                    }
                }
            }
            delay(3000)
        }
        /*
        output:
        Exception in thread "pool-1-thread-4 @coroutine#4" java.lang.IllegalArgumentException: Child 2 Error
            at programmer.zaman.now.coroutine.SupervisorJobTest$testSupervisorScopeFunction$1$1$1$2.invokeSuspend(SupervisorJobTest.kt:150)
            at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
            at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:106)
            at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)
            at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)
            at java.base/java.lang.Thread.run(Thread.java:829)
        Child 1 Done

        catatan:
        Output dari "Child 1 done" muncul berarti salah satu exception pada coroutine tidak akan mempengaruhi coroutine yg lainnya
        Jika menggunakan Supervisor function pada scope,jika terjadi error pada salah satu coroutine maka tidak akan mempengaruhi coroutine yg lainnya
        Jadi setiap coroutine child nya bersifat individu jika terdapat error pada salah satu coroutine maka tidak mempengaruhi coroutine yg lainnya
         */
    }

    @Test
    fun testJobExceptionHandler() {
        val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
            println("Error ${throwable.message}")
        }

        val dispatcher = Executors.newFixedThreadPool(10).asCoroutineDispatcher()
        val scope = CoroutineScope(dispatcher)

        runBlocking {
            val job = scope.launch {
                launch(exceptionHandler) { // not used
                    println("Job Child")
                    throw IllegalArgumentException("Child Error")
                }
            }

            job.join()
        }
    }

    @Test
    fun testSupervisorJobExceptionHandler() {
        val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
            println("Error ${throwable.message}")
        }

        val dispatcher = Executors.newFixedThreadPool(10).asCoroutineDispatcher()
        val scope = CoroutineScope(dispatcher)

        runBlocking {
            val job = scope.launch {
                supervisorScope {
                    launch(exceptionHandler) {
                        launch(exceptionHandler) { // not used
                            println("Job Child")
                            throw IllegalArgumentException("Child Error")
                        }
                    }
                }
            }

            job.join()
        }
    }
}