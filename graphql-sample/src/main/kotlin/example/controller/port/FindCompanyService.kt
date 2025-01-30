package example.controller.port

import example.controller.request.CompanyFilter
import example.service.domain.Company

interface FindCompanyService {

    fun findCompanyById(id: String): Company

    fun findAllCompanies(filter: CompanyFilter): List<Company>
}
