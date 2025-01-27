package example.controller.response

import example.service.domain.Company
import example.service.domain.CompanyState
import example.service.domain.Staff

data class CompanyResponse (
    val id: Int,
    val name: String,
    val alias: String,
    val state: CompanyState,
    val staffId: Int? = null
) {
    companion object {
        fun of(company: Company): CompanyResponse {
            return CompanyResponse (
                id = company.id,
                name = company.name,
                alias = company.alias,
                state = company.state,
                staffId = company.staffId
            )
        }
    }
}
