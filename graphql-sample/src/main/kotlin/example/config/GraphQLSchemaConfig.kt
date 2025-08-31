package example.config

import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeDefinitionRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GraphQLSchemaConfig {

    @Bean
    fun typeDefinitionRegistry(): TypeDefinitionRegistry {
        val schemaParser = SchemaParser()
        val typeRegistry = TypeDefinitionRegistry()

        val schemaDefinition = """
            type Query {
                findAllCompanies(filter: CompanyFilter): [Company]!
            }
            
            type Mutation {
                createCompany(createRequest: CreateCompanyRequest!): Company!
            }
            
            input CompanyFilter {
                statusIn: [CompanyState]
            }
            
            type Company {
                id: String!
                alias: String!
                state: CompanyState!
                employees: [Employee]!
            }
            
            type Employee {
                id: Int
                company: Company
                name: String
            }
            
            enum CompanyState {
                NORMAL
                PAUSED
                CLOSED
                DELETED
            }
            
            input CreateCompanyRequest {
                id: String!
                alias: String!
                employees: [EmployeeInput!]!
            }
            
            input EmployeeInput {
                name: String
            }
        """.trimIndent()

        typeRegistry.merge(schemaParser.parse(schemaDefinition))
        return typeRegistry
    }
}
