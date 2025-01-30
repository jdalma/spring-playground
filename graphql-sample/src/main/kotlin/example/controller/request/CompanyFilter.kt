package example.controller.request

import example.service.domain.CompanyState

data class CompanyFilter(
    val statusIn: List<CompanyState> = emptyList(),
)
