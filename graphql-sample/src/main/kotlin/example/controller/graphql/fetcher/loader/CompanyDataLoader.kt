package example.controller.graphql.fetcher.loader

import com.netflix.graphql.dgs.DgsDataLoader
import example.controller.port.FindCompanyService
import example.logger
import example.service.domain.Company
import org.dataloader.BatchLoader
import org.slf4j.Logger
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

@DgsDataLoader(name = CompanyDataLoader.COMPANY_LOADER_NAME)
class CompanyDataLoader (
    private val findCompanyService: FindCompanyService
): BatchLoader<String, Company> {

    private val logger: Logger = this::class.logger()

    companion object {
        const val COMPANY_LOADER_NAME = "company"
    }

    override fun load(companyIds: List<String>): CompletionStage<List<Company?>> {
        logger.info("[CompanyDataLoader.load] Thread name : ${Thread.currentThread().name}")
        return CompletableFuture.supplyAsync {
            logger.info("[CompanyDataLoader.load -> CompletableFuture.supplyAsync] Thread name : ${Thread.currentThread().name}")
            val companies = findCompanyService.findCompanyByIds(companyIds)
            companies.map { company ->
                companies.find { it.id == company.id }
            }
        }
    }
}
