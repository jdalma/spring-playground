package example

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DataAccessApplication

fun main(args: Array<String>) {
    runApplication<DataAccessApplication>(*args)
}

/**
 * 모든 Kotlin 클래스에서 SLF4J의 Logger 객체를 얻을 수 있다.
 */
inline fun <reified T> T.logger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}
