package example.controller.request

import example.service.domain.CompanyState

data class CompanyFilter(
    val nameOrAliasLike: String? = null,
    val state: CompanyState? = null,
)
