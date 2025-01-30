package example.controller.port

import example.service.domain.Employee
import example.service.model.CreateEmployee

interface CreateEmployeeService {

    fun create(request: List<CreateEmployee>): List<Employee>
}
