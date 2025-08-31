package example.controller.graphql.fetcher

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import example.controller.graphql.fetcher.loader.CompanyDataLoader
import example.controller.graphql.fetcher.loader.EmployeeDataLoader
import example.controller.port.FindCompanyService
import example.controller.request.CompanyFilter
import example.controller.response.Connection
import example.controller.response.DefaultConnection
import example.logger
import example.service.domain.Company
import example.service.domain.Employee
import org.slf4j.Logger
import java.lang.RuntimeException
import java.util.concurrent.CompletableFuture

@DgsComponent
class CompanyFetcher (
    private val findCompanyService: FindCompanyService
) {
    private val logger: Logger = this::class.logger()
    companion object {
        const val TYPE_NAME = "Company"
    }

    @DgsQuery
    fun findAllCompanies(@InputArgument filter: CompanyFilter): List<Company> {
        logger.info("[CompanyFetcher.findAllCompanies] Thread name : ${Thread.currentThread().name}")
        return findCompanyService.findAllCompanies(filter)
    }

    @DgsData(parentType = TYPE_NAME, field = "employees") // == @DgsQuery(field = "CompanyResponse")
    fun findEmployeesByCompanyId(environment: DgsDataFetchingEnvironment): CompletableFuture<List<Employee>> {
        logger.info("[CompanyFetcher.findEmployeesByCompanyId] Thread name : ${Thread.currentThread().name}")
        val companyResponse = environment.getSourceOrThrow<Company>()
        val dataLoader = environment.getDataLoader<String, List<Employee>>(EmployeeDataLoader.EMPLOYEE_LOADER_NAME)
            ?: throw RuntimeException("${EmployeeDataLoader.EMPLOYEE_LOADER_NAME} Loader가 존재하지 않습니다.")

        return dataLoader.load(companyResponse.id)
    }

    @DgsData(parentType = "Employee", field = "company")
    fun findCompanyByEmployee(environment: DgsDataFetchingEnvironment): CompletableFuture<Company> {
        logger.info("[CompanyFetcher.findCompanyByEmployee] Thread name : ${Thread.currentThread().name}")
        val employee = environment.getSourceOrThrow<Employee>()
        val dataLoader = environment.getDataLoader<String, Company>(CompanyDataLoader.COMPANY_LOADER_NAME)
            ?: throw RuntimeException("${CompanyDataLoader.COMPANY_LOADER_NAME} Loader가 존재하지 않습니다.")

        return dataLoader.load(employee.companyId)
    }
}
