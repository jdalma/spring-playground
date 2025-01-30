package example

import example.service.domain.Company
import example.service.domain.CompanyState
import example.service.domain.Employee
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BeanConfiguration {

    @Bean
    fun inMemoryEmployees(): Map<Int, Employee> = mapOf(
        1 to Employee(id = 1, companyId = "별빛기술", name = "하늘솔"),
        2 to Employee(id = 2, companyId = "하늘정보", name = "구름별"),
        3 to Employee(id = 3, companyId = "구름소프트", name = "바다람"),
        4 to Employee(id = 4, companyId = "바다시스템", name = "푸른꽃"),
        5 to Employee(id = 5, companyId = "별빛기술", name = "달빛솔"),
        6 to Employee(id = 6, companyId = "별빛기술", name = "은하수"),
        7 to Employee(id = 7, companyId = "별빛기술", name = "별가람"),
        8 to Employee(id = 8, companyId = "하늘정보", name = "해나래")
    )

    @Bean
    fun inMemoryCompanies(employees: Map<Int, Employee>): Map<String, Company> = mapOf(
        "별빛기술" to Company(
            id = "별빛기술",
            alias = "STAR",
            state = CompanyState.NORMAL
        ),
        "하늘정보" to Company(
            id = "하늘정보",
            alias = "SKYI",
            state = CompanyState.NORMAL
        ),
        "구름소프트" to Company(
            id = "구름소프트",
            alias = "CLOD",
            state = CompanyState.PAUSED
        ),
        "바다시스템" to Company(
            id = "바다시스템",
            alias = "SEAS",
            state = CompanyState.NORMAL
        ),
        "달빛데이터" to Company(
            id = "달빛데이터",
            alias = "MOON",
            state = CompanyState.CLOSED
        )
    )
}
