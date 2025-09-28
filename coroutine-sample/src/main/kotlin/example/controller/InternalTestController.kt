package example.controller

import kotlinx.coroutines.delay
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@RestController
@RequestMapping("/internal")
class InternalTestController {

    @GetMapping("/large-response")
    fun largeResponse(): Flux<String> {
        return Flux.interval(Duration.ofMillis(100))
            .take(100)
            .map { "Data chunk $it\n" }
    }

    @GetMapping("/close-early")
    fun closeEarly(): Mono<ResponseEntity<String>> {
        return Mono.just(ResponseEntity.status(HttpStatus.OK).body("Starting response"))
            .delayElement(Duration.ofMillis(100))
            .then(Mono.error(RuntimeException("Connection closed by server")))
    }

    @GetMapping("/chunked-close", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun chunkedClose(): Flux<String> {
        return Flux.concat(
            Flux.just("chunk1", "chunk2", "chunk3")
                .delayElements(Duration.ofMillis(100)),
            Flux.error(RuntimeException("Premature close"))
        )
    }

    @GetMapping("/delay/{seconds}")
    suspend fun delayResponse(@PathVariable seconds: Long): String {
        delay(seconds * 1000)
        return "delay api success. seconds=$seconds"
    }

    @GetMapping("/streaming-close/{chunks}")
    fun streamingClose(@PathVariable chunks: Int): Flux<String> {
        return Flux.interval(Duration.ofMillis(100))
            .take(chunks.toLong())
            .map { i -> "Chunk $i of $chunks\n" }
            .concatWith(Flux.error(RuntimeException("Premature connection close during streaming")))
    }
}
