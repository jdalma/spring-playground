package example.batch.repository

import example.batch.entity.TestEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TestEntityRepository : JpaRepository<TestEntity, Long>