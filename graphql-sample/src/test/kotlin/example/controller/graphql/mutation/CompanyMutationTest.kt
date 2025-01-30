package example.controller.graphql.mutation

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.graphql.dgs.DgsQueryExecutor
import com.netflix.graphql.dgs.test.EnableDgsTest
import example.controller.graphql.fetcher.CompanyConnection
import example.controller.graphql.fetcher.CompanyWithEmployees
import example.service.domain.CompanyState
import graphql.ExecutionResult
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@EnableDgsTest
class CompanyMutationTest {

    @Autowired
    private lateinit var dgsQueryExecutor: DgsQueryExecutor
    private val objectMapper = ObjectMapper()

    @Test
    fun `should create company with employees`() {
        // given
        val query = """
           mutation CreateCompany(${'$'}request: CreateCompanyRequest!) {
               createCompany(createCompanyRequest: ${'$'}request) {
                   id
                   alias
                   state
                   employees {
                       id
                       name
                   }
               }
           }
       """.trimIndent()

        val variables = mapOf(
            "request" to mapOf(
                "id" to "그냥기술",
                "alias" to "MOON",
                "employees" to listOf(
                    mapOf("name" to "임직원"),
                    mapOf("name" to "정직원")
                )
            )
        )

        // when
        val rawResponse = dgsQueryExecutor.execute(query, variables)
        val response = convertResponse(rawResponse, "convertResponse")

        // then
        with(response) {
            alias shouldBe "MOON"
            state shouldBe CompanyState.NORMAL
            id shouldNotBe null

            employees shouldHaveSize 2
            employees.map { it.name } shouldContainAll listOf("임직원", "정직원")
            employees.forEach { it.id shouldNotBe null }
        }
    }

    private fun convertResponse(result: ExecutionResult, name: String): CompanyWithEmployees {
        val rawData = result.getData<Map<String, Any>>()[name]
        return objectMapper.convertValue(rawData, CompanyWithEmployees::class.java)
    }
}
