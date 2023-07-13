package example.circuitBreaker

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import jdk.jfr.Percentage
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("client-circuit")
class ClientController (
    val translateCircuitBreaker : CircuitBreaker
) {

    @GetMapping
    fun client(
        @RequestParam message: String,
        @RequestParam(defaultValue = "0") delay: Int,
        @RequestParam(defaultValue = "100") percentage: Int
    ) : String {
        return message
    }
}
