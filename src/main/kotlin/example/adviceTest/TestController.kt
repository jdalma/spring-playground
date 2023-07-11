package example.adviceTest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/adviceTest")
class TestController {

    @GetMapping
    fun hello() : String {
        return "hello"
    }
}
