package example.controller.graphql.fetcher

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import example.controller.graphql.fetcher.loader.EmployeeDataLoader
import example.controller.port.FindCompanyService
import example.controller.request.CompanyFilter
import example.controller.response.Connection
import example.controller.response.DefaultConnection
import example.service.domain.Company
import example.service.domain.Employee
import java.lang.RuntimeException
import java.util.concurrent.CompletableFuture

@DgsComponent
class CompanyFetcher (
    private val findCompanyService: FindCompanyService
) {
    companion object {
        const val TYPE_NAME = "CompanyResponse"
    }

    @DgsQuery
    fun findAllCompanies(@InputArgument filter: CompanyFilter): Connection<Company> {
        val items = findCompanyService.findAllCompanies(filter)
        return DefaultConnection.of(
            items = items,
            totalCount = items.size
        )
    }

    @DgsData(parentType = TYPE_NAME, field = "employees") // == @DgsQuery(field = "CompanyResponse")
    fun findEmployeesByCompanyId(environment: DgsDataFetchingEnvironment): CompletableFuture<List<Employee>> {
        val companyResponse = environment.getSource<Company>()
            ?: throw RuntimeException("Company가 존재하지 않습니다.")
        val dataLoader = environment.getDataLoader<String, List<Employee>>(EmployeeDataLoader.EMPLOYEE_LOADER_NAME)
            ?: throw RuntimeException("${EmployeeDataLoader.EMPLOYEE_LOADER_NAME} Loader가 존재하지 않습니다.")

        return dataLoader.load(companyResponse.id)
    }
}
