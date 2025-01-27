package example

import example.service.domain.Company
import example.service.domain.CompanyState
import example.service.domain.Staff
import example.service.exception.ResourceNotFoundException
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.lang.RuntimeException

@Configuration
class BeanConfiguration {

    @Bean
    fun inMemoryStaffs(): Map<Int, Staff> = initStaffs()

    @Bean
    fun inMemoryCompanies(staffs: Map<Int, Staff>): Map<Int, Company> = initCompanies(staffs)

    private fun initStaffs(): Map<Int, Staff> {
        return (1 .. 20).map { index ->
            Staff(index, "${index}번 대표")
        }.associateBy { it.id }
    }

    private fun initCompanies(staffs: Map<Int, Staff>): Map<Int, Company> {
        return (1 .. 20).map { index ->
            Company (
                index,
                "${index}번 회사",
                "${index}번",
                CompanyState.NORMAL,
                index
            )
        }.associateBy { it.id }
    }
}
