package example.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.Thread.sleep

@RestController
class TestController(private val coroutineScope: CoroutineScope) {

    private val scope = CoroutineScope(Dispatchers.IO)

    @GetMapping("/test")
    fun test(): String {
        println(scope.coroutineContext.job.isActive)
        println(scope.coroutineContext.job.isCancelled)
        println(scope.coroutineContext.job.isCompleted)
        scope.launch {
            // business logic
            println("start coroutine")
            throw RuntimeException("비즈니스 로직 런타임 예외 발생!!!")
            println("end coroutine")
        }
        println(scope.coroutineContext.job.isActive)
        println(scope.coroutineContext.job.isCancelled)
        println(scope.coroutineContext.job.isCompleted)
        return "success"
    }

    @GetMapping("/test2")
    suspend fun test2(): String {
        println("start test2")
        delay(1000)
        return "success"
    }
}
