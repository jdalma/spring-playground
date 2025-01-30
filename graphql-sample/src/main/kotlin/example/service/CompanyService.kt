package example.service

import example.controller.port.CreateCompanyService
import example.controller.port.FindCompanyService
import example.controller.request.CompanyFilter
import example.controller.request.CreateCompanyRequest
import example.service.domain.Company
import example.service.domain.CompanyState
import example.service.exception.ResourceNotFoundException
import org.springframework.stereotype.Service

@Service
class CompanyService (
    private val companies: MutableMap<String, Company>
): FindCompanyService, CreateCompanyService {

    override fun findCompanyById(id: String): Company {
        return companies[id] ?: throw ResourceNotFoundException("회사를 찾을 수 없습니다. [$id]")
    }

    override fun findAllCompanies(filter: CompanyFilter): List<Company> {
        return companies.values
            .applyStateFilter(filter.statusIn)
    }

    override fun createCompany(request: CreateCompanyRequest): Company {
        val company = Company(request.id, request.alias, CompanyState.NORMAL)
        companies[request.id] = company
        return company
    }

    private fun Collection<Company>.applyStateFilter(states: List<CompanyState>) =
        filter { company -> states.contains(company.state) }
}
