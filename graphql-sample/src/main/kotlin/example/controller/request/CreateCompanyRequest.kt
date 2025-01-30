package example.controller.request

data class CreateCompanyRequest (
    val id: String,
    val alias: String,
    val employees: List<EmployeeInput>
)

data class EmployeeInput (
    val name: String
)
