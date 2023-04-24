package example.events.event

import java.time.LocalDateTime


data class CustomEvent(
    private val localDateTime: LocalDateTime,
    private val msg: String
)