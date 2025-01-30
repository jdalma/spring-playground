package example.controller.graphql.mutation

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import example.controller.port.CreateCompanyService
import example.controller.port.CreateEmployeeService
import example.controller.request.CreateCompanyRequest
import example.service.domain.Company
import example.service.model.CreateEmployee

@DgsComponent
class CompanyMutation (
    private val createCompanyService: CreateCompanyService,
    private val createEmployeeService: CreateEmployeeService
) {

    @DgsMutation
    fun createCompany(createRequest: CreateCompanyRequest): Company {
        val company = createCompanyService.createCompany(createRequest)
        val createEmployee = createRequest.employees.map {
            CreateEmployee(company.id, it.name)
        }
        createEmployeeService.create(createEmployee)
        return company
    }
}
