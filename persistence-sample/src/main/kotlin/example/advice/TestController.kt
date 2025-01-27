package example.advice

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.lang.RuntimeException

@RestController
@RequestMapping("/person")
class TestController {

    @PostMapping
    fun save(@RequestBody person: Person) : String {
        throw Throwable("exception !!!")
    }
}

data class Person(
    val name: String,
    val age: Int
)
