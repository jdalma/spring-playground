package example.service

import example.controller.port.FindStaffService
import example.service.domain.Staff
import org.springframework.stereotype.Service

@Service
class StaffService (
    private val inMemoryStaffs: Map<Int, Staff>
): FindStaffService {

    override fun findStaffsByIds(ids: List<Int>): List<Staff> {
        return ids.mapNotNull { inMemoryStaffs[it] }
    }
}
