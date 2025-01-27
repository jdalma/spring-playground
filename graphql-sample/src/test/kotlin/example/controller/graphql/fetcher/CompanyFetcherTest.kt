package example.controller.graphql.fetcher

import com.netflix.graphql.dgs.DgsQueryExecutor
import com.netflix.graphql.dgs.test.EnableDgsTest
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest


@SpringBootTest(classes = [ CompanyFetcher::class ])
@EnableDgsTest
class CompanyFetcherTest (
    private val dgsQueryExecutor: DgsQueryExecutor
) {

    @Test
    fun companies() {
        @Language("GraphQL")
        val query = """
            query findAllCompanies(${'$'}filter: CompanyFilter) {
            	findAllCompanies(filter: ${'$'}filter){
                ...companyConnection
              }
            }

            fragment companyConnection on CompanyConnection {
                totalCount
                edges {
                  cursor
                  node {
                    id
                    name
                    alias
                    state
                    staff {
                      name
                    }
                  }
                }
              }
        """.trimIndent()
        val variables = mapOf(
            "nameOrAliasLike" to null,
            "statusIn" to "NORMAL"
        )
        dgsQueryExecutor.execute(query, variables)
    }
}
