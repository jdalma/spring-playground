package example.controller.graphql.fetcher.loader

import com.netflix.graphql.dgs.DgsDataLoader
import example.controller.graphql.fetcher.loader.StaffLoader.Companion.STAFF_LOADER_NAME
import example.controller.port.FindStaffService
import example.service.domain.Staff
import org.dataloader.BatchLoader
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

@DgsDataLoader(name = STAFF_LOADER_NAME)
class StaffLoader (
    private val findStaffService: FindStaffService
): BatchLoader<Int, Staff> {

    companion object {
        const val STAFF_LOADER_NAME = "staff"
    }

    override fun load(keys: List<Int>): CompletionStage<List<Staff>> {
        val result = findStaffService.findStaffsByIds(keys)
        return CompletableFuture.completedFuture(result)
    }
}
