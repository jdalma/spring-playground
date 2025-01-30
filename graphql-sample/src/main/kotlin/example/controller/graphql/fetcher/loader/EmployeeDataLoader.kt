package example.controller.graphql.fetcher.loader

import com.netflix.graphql.dgs.DgsDataLoader
import example.controller.graphql.fetcher.loader.EmployeeDataLoader.Companion.EMPLOYEE_LOADER_NAME
import example.controller.port.FindEmployeeService
import example.service.domain.Employee
import org.dataloader.BatchLoader
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

@DgsDataLoader(name = EMPLOYEE_LOADER_NAME)
class EmployeeDataLoader (
    private val findEmployeeService: FindEmployeeService
): BatchLoader<String, List<Employee>> {

    companion object {
        const val EMPLOYEE_LOADER_NAME = "employees"
    }

    override fun load(keys: List<String>): CompletionStage<List<List<Employee>>> {
        // 부모 관계의 주인을 알 수 없는 경우 : The size of the promised values MUST be the same size as the key list
        // val result = findEmployeeService.findEmployeesByIds(keys.flatten())

        val employees = findEmployeeService.findEmployeesByCompanyIds(keys)
        val filteredEmployees = keys.map { companyId ->
            employees.filter { it.companyId == companyId }
        }
        return CompletableFuture.completedFuture(filteredEmployees)
    }
}
