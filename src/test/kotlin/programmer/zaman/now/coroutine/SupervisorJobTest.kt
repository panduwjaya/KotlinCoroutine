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

    /*
    ========== Exception Handler di Job vs Supervisor Job ==========

    Exception Handler di Job dan Supervisor Job
    Exception handler di Job ataupun di Supervisor Job secara default akan di propagate ke parent nya,Artinya ketika terjadi Exception maka permasalahan tersebut tidak diselesaikan pada child nya
    Namun akan diselesaikan pada parent nya dengan cara di propagate Exception ke parentnya agar permasalahn tsb dapat diselesaikan pada parent
    Artinya jika kita membuat CoroutineExceptionHandler, kita harus membuatnya di parent, tidak bisa di coroutine child nya.
    Jika kita menambahkan exception handler di coroutine child nya, maka itu tidak akan pernah digunakan

    catatan:
    solusi agar exceptionHandler dapat digunakan pada child menggunakan Exception Handler dengan supervisorScope
    Namun sebenarnya ExceptionHandler hanya dapat digunakan pada parent bukan child,pada supervisorScope hanya bertugas merekayasa menggantikan parent pada child
     */

    // Contoh kode exceptionHandler yg salah
    // Alasan kode ini salah karena menempatkan ExceptionHandler pada child bukan pada parent sedangkan ExceptionHandler hanya bisa dieksekusi pada parent
    @Test
    fun testJobExceptionHandler() {
        val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
            println("Error ${throwable.message}")
        }

        val dispatcher = Executors.newFixedThreadPool(10).asCoroutineDispatcher()
        val scope = CoroutineScope(dispatcher)

        runBlocking {
            val job = scope.launch {
                launch(exceptionHandler) { // not used,artinya ExceptionHandler ini tidak digunakan atau sia sia
                    println("Job Child")
                    throw IllegalArgumentException("Child Error")
                }
            }

            job.join()
        }
        /*
        output:
        Job Child
        Exception in thread "pool-1-thread-2 @coroutine#3" java.lang.IllegalArgumentException: Child Error
            at programmer.zaman.now.coroutine.SupervisorJobTest$testJobExceptionHandler$1$job$1$1.invokeSuspend(SupervisorJobTest.kt:199)
            at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
            at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:106)
         */
    }

    /*
    Exception Handler dengan supervisorScope
    Salah satu cara agar exception handler bisa dilakukan di coroutine child adalah dengan menggunakan supervisorScope
    Saat menggunakan supervisorScope, maka exception bisa di gunakan di parent coroutine di supervisorScope, atau sebenarnya coroutine child di scope yang ada diatas nya
    Tapi ingan jika terjadi error di child nya coroutine yang ada di supervisorScope, maka tetap akan di propagate ke parent coroutine di cupervisorScope

    catatan:
    solusi agar exceptionHandler dapat digunakan pada child menggunakan Exception Handler dengan supervisorScope
    Namun sebenarnya ExceptionHandler hanya dapat digunakan pada parent bukan child,pada supervisorScope hanya bertugas merekayasa menggantikan parent pada child

    example:
    cara peletakan exceptionHandler tidak sembarangan
    Artinya untuk meletakan exceptionHandler harus berada tepat diatas child yang akan menggunakan exceptionHandler
    Tidak meletakan exceptionHandler tepat pada child yg akan dieksekusi

    contoh exceptionHandler yg not used:
    runBlocking {
            val job = scope.launch {
                supervisorScope {
                    launch(exceptionHandler) {
                        launch(exceptionHandler) { // not used
                            println("Job Child")
                            throw IllegalArgumentException("Child Error")
                        }


    contoh exceptionHandler yg used:
    runBlocking {
            val job = scope.launch {
                supervisorScope {
                    launch(exceptionHandler) {
                        launch { // used
                            println("Job Child")
                            throw IllegalArgumentException("Child Error")
                        }

    Dengan adanya supervisorScope maka exceptionHandler dapat digunakan pada scope child tanpa peletakan sembarangan
    Yaitu peletekan harus diletakan pada tepat diatas child yang akan terkena exception bukan pada child yang terkena exception karena itu tidak berguna
     */

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
                    launch(exceptionHandler) { // berperan sebagai pengganti parent pada child,padahal bukan parent sebenarnya
                        launch(exceptionHandler) { // exceptionHandler not used,artinya yg digunakan hanya exceptionHandler yg berada tepat diatas launch
                            println("Job Child")
                            throw IllegalArgumentException("Child Error")
                        }
                    }
                }
            }

            job.join()
        }
        /*
        output:
        Job Child
        Error Child Error

        catatan:
        Dengan adanya supervisorScope pada child kita dapat menggunakan ExceptionHandler pada child dan mampu menghandle jika terjadi exception menggunakan ExceptionHandler di child
         */
    }
}