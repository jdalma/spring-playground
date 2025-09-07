package example.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.Thread.sleep

@RestController
class TestController {

    private val scope = CoroutineScope(Dispatchers.IO)

    @GetMapping("/test")
    fun test(): String {
        sleep(10000)
        return "success"
    }

    @GetMapping("/test2")
    suspend fun test2(): String {
        println("start test2")
        return "success"
    }
}
