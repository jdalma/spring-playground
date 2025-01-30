package example.service.domain

data class Company (
    val id: String,
    val alias: String,
    val state: CompanyState
)

enum class CompanyState {
    NORMAL,
    PAUSED,
    CLOSED,
    DELETED
}
