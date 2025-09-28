package example.controller

import example.service.PrematureCloseExceptionService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/premature")
class PrematureCloseExceptionController(
    private val service: PrematureCloseExceptionService
) {

    @GetMapping("/case1")
    suspend fun testCase1(): String {
        return service.case1LargeResponseTerminatedEarly()
    }

    @GetMapping("/case2/{delay}")
    suspend fun testCase2(
        @PathVariable delay: Int
    ): String {
        return service.case2ConnectionIdleTimeout(delay)
    }

    @GetMapping("/case3")
    suspend fun testCase3(): String {
        return service.case3ServerClosesEarly()
    }
}
