package example.controller.graphql.fetcher

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.netflix.graphql.dgs.DgsQueryExecutor
import com.netflix.graphql.dgs.test.EnableDgsTest
import example.service.domain.CompanyState
import example.service.domain.Employee
import graphql.ExecutionResult
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@EnableDgsTest
class CompanyFetcherTest {

    @Autowired
    private lateinit var dgsQueryExecutor: DgsQueryExecutor
    private val objectMapper = jacksonObjectMapper()
    // @Test
    // @DisplayName("NORMAL, PAUSED 상태인 회사를 검색한다.")
    fun searchCompaniesByStatus() {
        // given
        val query = """
           query GetAllCompanies {
               findAllCompanies(filter: { statusIn: [NORMAL, PAUSED] }) {
                   totalCount
                   responses {
                       id
                       alias
                       state
                       employees {
                           id
                           companyId
                           name
                       }
                   }
               }
           }
        """.trimIndent()

        // when
        val result: ExecutionResult = dgsQueryExecutor.execute(query)
        val response = convertResponse(result, "findAllCompanies")

        // then
        with(response) {
            totalCount shouldBe 4
            responses shouldHaveSize 4

            with(responses.first()) {
                id shouldBe "별빛기술"
                alias shouldBe "STAR"
                state shouldBe CompanyState.NORMAL

                employees shouldHaveSize 4
                with(employees.first()) {
                    id shouldBe 1
                    companyId shouldBe "별빛기술"
                    name shouldBe "하늘솔"
                }
            }
        }
    }

    private fun convertResponse(result: ExecutionResult, name: String): CompanyConnection {
        val rawData = result.getData<Map<String, Any>>()[name]
        return objectMapper.convertValue(rawData, CompanyConnection::class.java)
    }
}

data class CompanyConnection(
    val totalCount: Int,
    val responses: List<CompanyWithEmployees>
)

data class CompanyWithEmployees(
    val id: String,
    val alias: String,
    val state: CompanyState,
    val employees: List<Employee>
)
