package example.service

import example.controller.port.CreateEmployeeService
import example.controller.port.FindEmployeeService
import example.service.domain.Employee
import example.service.model.CreateEmployee
import org.springframework.stereotype.Service

@Service
class EmployeeService (
    private val inMemoryEmployee: MutableMap<Int, Employee>
): FindEmployeeService, CreateEmployeeService {

    override fun findEmployeesByIds(ids: List<Int>): List<Employee> {
        return ids.mapNotNull { inMemoryEmployee[it] }
    }

    override fun findEmployeesByCompanyIds(companyIds: List<String>): List<Employee> {
        return inMemoryEmployee.values
            .filter { companyIds.contains(it.companyId) }
    }

    override fun create(request: List<CreateEmployee>): List<Employee> {
        var maxId = inMemoryEmployee.keys.max() + 1
        return request.map {
            Employee(
                id = maxId,
                companyId = it.companyId,
                name = it.name
            ).also {
                inMemoryEmployee[maxId++] = it
            }
        }
    }
}
