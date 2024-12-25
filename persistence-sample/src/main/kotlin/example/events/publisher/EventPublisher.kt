package example.events.publisher

import example.events.event.CustomEvent
import example.logger
import org.slf4j.Logger
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component


@Component
class EventPublisher(
    private val publisher: ApplicationEventPublisher
) {

    val log: Logger = EventPublisher::class.logger()

    fun eventPublish(event: CustomEvent) {
        log.info("Publisher sendEvent : {} {}", event , Thread.currentThread().id)
        publisher.publishEvent(event)
    }

    fun stringPublish(msg: String) {
        log.info("Publisher sendMsg : {} {}", msg , Thread.currentThread().id)
        publisher.publishEvent(msg)
    }
}