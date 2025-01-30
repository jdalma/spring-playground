package example.controller.port

import example.controller.request.CreateCompanyRequest
import example.service.domain.Company

interface CreateCompanyService {

    fun createCompany(request: CreateCompanyRequest): Company
}
