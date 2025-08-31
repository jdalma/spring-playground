package example.controller.graphql.fetcher.loader

import com.netflix.graphql.dgs.DgsDataLoader
import example.controller.graphql.fetcher.loader.EmployeeDataLoader.Companion.EMPLOYEE_LOADER_NAME
import example.controller.port.FindEmployeeService
import example.logger
import example.service.domain.Employee
import org.dataloader.BatchLoader
import org.slf4j.Logger
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

@DgsDataLoader(name = EMPLOYEE_LOADER_NAME)
class EmployeeDataLoader (
    private val findEmployeeService: FindEmployeeService
): BatchLoader<String, List<Employee>> {

    private val logger: Logger = this::class.logger()

    companion object {
        const val EMPLOYEE_LOADER_NAME = "employee"
    }

    override fun load(keys: List<String>): CompletionStage<List<List<Employee>>> {
        // 부모 관계의 주인을 알 수 없는 경우 : The size of the promised values MUST be the same size as the key list
        // val result = findEmployeeService.findEmployeesByIds(keys.flatten())
        logger.info("[EmployeeDataLoader.load] Thread name : ${Thread.currentThread().name}")
        return CompletableFuture.supplyAsync {
            logger.info("[EmployeeDataLoader.load -> CompletableFuture.supplyAsync] Thread name : ${Thread.currentThread().name}")
            val employees = findEmployeeService.findEmployeesByCompanyIds(keys)
            keys.map { companyId ->
                employees.filter { it.companyId == companyId }
            }
        }
    }
}
