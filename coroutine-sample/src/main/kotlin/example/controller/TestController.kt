package example.controller

import kotlinx.coroutines.delay
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.Thread.sleep

@RestController
class TestController {

    @GetMapping("/test")
    fun test(): String {
        sleep(10000)
        return "success"
    }
}
