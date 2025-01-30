package example.controller.rest

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/test")
class TestController {

    @GetMapping
    fun getTests(): ResponseEntity<List<TestResponse>> {
        return ResponseEntity.ok(listOf(
            TestResponse("test1", "content1"),
            TestResponse("test2", "content2")
        ))
    }

    @GetMapping("/{id}")
    fun getTest(@PathVariable id: String): ResponseEntity<TestResponse> {
        return ResponseEntity.ok(
            TestResponse(id, "content for $id")
        )
    }

    @PostMapping
    fun createTest(@RequestBody request: CreateTestRequest): ResponseEntity<TestResponse> {
        val response = TestResponse(request.name, request.content)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/{id}")
    fun updateTest(
        @PathVariable id: String,
        @RequestBody request: UpdateTestRequest
    ): ResponseEntity<TestResponse> {
        val response = TestResponse(id, request.content)
        return ResponseEntity.ok(response)
    }
}

data class TestResponse(
    val name: String,
    val content: String
)

data class CreateTestRequest(
    val name: String,
    val content: String
)

data class UpdateTestRequest(
    val content: String
)
