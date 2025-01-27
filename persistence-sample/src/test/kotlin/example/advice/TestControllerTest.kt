package example.advice

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print

@WebMvcTest(TestController::class)
class TestControllerTest (
    @Autowired
    private val mockMvc: MockMvc
) {
    @Test
    fun `should handle Throwable exception`() {
        mockMvc.perform(
            get("/advice")
                // .param("exception", "throwable")
        )
            .andExpect(status().isInternalServerError)
            .andDo(print())
    }


    @Test
    fun `should handle RuntimeException`() {
        mockMvc.perform(
            get("/advice")
                .param("exception", "runtime")
        )
            .andExpect(status().isInternalServerError)  // 500 에러 기대
            .andDo(print())
    }
}
