package example.events

import example.events.event.CustomEvent
import example.events.publisher.EventPublisher
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/event")
class EventController (
    private val eventPublisher: EventPublisher
) {

    @GetMapping
    fun hello() : String {
        return "hello"
    }

    @PostMapping
    fun sendEvent() : String {
        eventPublisher.eventPublish(CustomEvent(LocalDateTime.now(), "CustomEvent"))
        eventPublisher.stringPublish("message")
        return "success"
    }
}