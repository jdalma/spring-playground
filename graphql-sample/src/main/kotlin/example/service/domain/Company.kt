package example.service.domain

data class Company (
    val id: Int,
    val name: String,
    val alias: String,
    val state: CompanyState,
    val staffId: Int? = null
)

enum class CompanyState {
    NORMAL,
    PAUSED,
    CLOSED,
    DELETED
}
