package example.controller

//import example.events.LoggingAspect
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers


@SpringBootTest
@AutoConfigureMockMvc
class EventControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun helloTest() {
        val uri: String = "/event"
        mockMvc.perform(MockMvcRequestBuilders.get(uri))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string("hello"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun eventTest() {
        val uri: String = "/event"
        mockMvc.perform(MockMvcRequestBuilders.post(uri))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().string("hello"))
            .andDo(MockMvcResultHandlers.print())
    }
}