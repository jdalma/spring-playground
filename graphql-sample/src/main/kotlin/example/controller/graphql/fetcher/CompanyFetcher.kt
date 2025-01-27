package example.controller.graphql.fetcher

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import example.controller.graphql.fetcher.loader.StaffLoader
import example.controller.port.FindCompanyService
import example.controller.request.CompanyFilter
import example.controller.response.CompanyResponse
import example.controller.response.Connection
import example.controller.response.DefaultConnection
import example.service.domain.Staff
import java.lang.RuntimeException
import java.util.concurrent.CompletableFuture

@DgsComponent
class CompanyFetcher (
    private val findCompanyService: FindCompanyService
) {
    companion object {
        const val TYPE_NAME = "CompanyResponse"
    }

    @DgsQuery
    fun findAllCompanies(@InputArgument filter: CompanyFilter): Connection<CompanyResponse> {
        val items = findCompanyService.findAllCompanies(filter).map { CompanyResponse.of(it) }
        return DefaultConnection.of(
            items = items,
            totalCount = items.size
        )
    }

    @DgsData(parentType = TYPE_NAME) // == @DgsQuery(field = "CompanyResponse")
    fun staff(environment: DgsDataFetchingEnvironment): CompletableFuture<Staff> {
        val staffId = environment.getSource<CompanyResponse>()?.staffId
            ?: throw RuntimeException("Company가 존재하지 않습니다.")
        val dataLoader = environment.getDataLoader<Int, Staff>(StaffLoader.STAFF_LOADER_NAME)
            ?: throw RuntimeException("${StaffLoader.STAFF_LOADER_NAME} Loader가 존재하지 않습니다.")

        return dataLoader.load(staffId)
    }
}
