package example.controller.port

import example.service.domain.Employee

interface FindEmployeeService {

    fun findEmployeesByIds(ids: List<Int>): List<Employee>
    fun findEmployeesByCompanyIds(companyIds: List<String>): List<Employee>
}
