package example.controller.port

import example.service.domain.Staff

interface FindStaffService {

    fun findStaffsByIds(ids: List<Int>): List<Staff>
}
