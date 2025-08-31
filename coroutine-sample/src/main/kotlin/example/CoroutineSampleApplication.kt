package example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CoroutineSampleApplication

fun main(args: Array<String>) {
    runApplication<CoroutineSampleApplication>(*args)
}