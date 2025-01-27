package example.service

import example.controller.port.CreateCompanyService
import example.controller.port.FindCompanyService
import example.controller.request.CompanyFilter
import example.service.domain.Company
import example.service.domain.CompanyState
import example.service.exception.ResourceNotFoundException
import org.springframework.stereotype.Service

@Service
class CompanyService (
    private val companies: Map<Int, Company>
): FindCompanyService, CreateCompanyService {

    override fun findCompanyById(id: Int): Company {
        return companies[id] ?: throw ResourceNotFoundException("회사를 찾을 수 없습니다. [$id]")
    }

    override fun findAllCompanies(filter: CompanyFilter): List<Company> {
        return companies.values
            .applyNameFilter(filter.nameOrAliasLike)
            .applyStateFilter(filter.state)
    }

    private fun Collection<Company>.applyNameFilter(nameOrAlias: String?) =
        filter { company -> nameOrAlias?.let { company.name.contains(it) } ?: true }

    private fun Collection<Company>.applyStateFilter(state: CompanyState?) =
        filter { company -> state?.let { company.state == it } ?: true }
}
