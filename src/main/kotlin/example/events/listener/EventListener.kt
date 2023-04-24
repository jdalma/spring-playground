package example.events.listener

import example.events.event.CustomEvent
import example.logger
import org.slf4j.Logger
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class EventListener {

    val log: Logger = EventListener::class.logger()

    @Async
    @EventListener
    fun sendEvent(event: CustomEvent) {
        log.info("Listener sendEvent : {} {}", event , Thread.currentThread().id)
    }

    @EventListener
    fun sendMsg(msg: String) {
        log.info("Listener sendMsg : {} {}", msg , Thread.currentThread().id)
    }
}