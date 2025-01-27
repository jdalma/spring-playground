package example.advice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AdviceTestApplication

fun main(args: Array<String>) {
    runApplication<AdviceTestApplication>(*args)
}
