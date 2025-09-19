package example.batch

import example.batch.entity.TestEntity
import example.batch.repository.TestEntityRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.TestPropertySource
import org.springframework.transaction.annotation.Transactional
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.time.LocalDateTime
import kotlin.system.measureTimeMillis

data class TestResult(
    val technology: String,
    val operation: String,
    val batchSize: Int,
    val timeMs: Long,
    val opsPerSecond: Double
)

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = [
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.jpa.properties.hibernate.jdbc.batch_size=100",
    "spring.jpa.properties.hibernate.order_inserts=true",
    "spring.jpa.properties.hibernate.order_updates=true",
    "spring.jpa.show-sql=false"
])
class JpaVsJdbcPerformanceTest {
    
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate
    
    @Autowired
    private lateinit var testEntityRepository: TestEntityRepository
    
    @PersistenceContext
    private lateinit var entityManager: EntityManager
    
    private val results = mutableListOf<TestResult>()
    
    @BeforeEach
    fun setUp() {
        createTable()
        cleanupData()
        results.clear()
    }
    
    private fun createTable() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS test_entity")
        jdbcTemplate.execute("""
            CREATE TABLE test_entity (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                amount INT NOT NULL,
                description VARCHAR(500) NOT NULL,
                created_at TIMESTAMP NOT NULL,
                updated_at TIMESTAMP NOT NULL
            )
        """)
    }
    
    private fun cleanupData() {
        jdbcTemplate.execute("DELETE FROM test_entity")
    }
    
    private fun generateTestEntities(count: Int): List<TestEntity> {
        return (1..count).map { i ->
            TestEntity(
                name = "Entity_$i",
                amount = i,
                description = "Sample description for entity $i with some realistic content to simulate real-world data size",
                createdAt = LocalDateTime.now().minusDays((i % 30).toLong()),
                updatedAt = LocalDateTime.now()
            )
        }
    }
    
    @Test
    @Transactional
    fun runJpaVsJdbcPerformanceComparison() {
        val batchSizes = listOf(100, 500, 1000, 2500, 5000)
        
        println("\n" + "=".repeat(80))
        println(" JPA vs JdbcTemplate BATCH PERFORMANCE COMPARISON ")
        println("=".repeat(80))
        
        for (size in batchSizes) {
            println("\n" + "-".repeat(60))
            println(" Batch Size: $size")
            println("-".repeat(60))
            
            testInsertOperations(size)
            testUpdateOperations(size)
        }
        
        printDetailedSummary()
    }
    
    private fun testInsertOperations(batchSize: Int) {
        println("\n📊 INSERT Operations:")
        
        // JDBC Single Insert
        val jdbcSingle = measureJdbcSingleInsert(batchSize)
        printTestResult("JDBC Single Insert", jdbcSingle)
        
        // JDBC Batch Insert
        val jdbcBatch = measureJdbcBatchInsert(batchSize)
        printTestResult("JDBC Batch Insert", jdbcBatch)
        
        // JPA Single Persist
        val jpaSingle = measureJpaSingleInsert(batchSize)
        printTestResult("JPA Single Persist", jpaSingle)
        
        // JPA Batch Persist
        val jpaBatch = measureJpaBatchInsert(batchSize)
        printTestResult("JPA Batch Persist", jpaBatch)
        
        // Performance comparison
        val improvement = if (jdbcSingle.timeMs > 0 && jdbcBatch.timeMs > 0) {
            jdbcSingle.timeMs.toDouble() / jdbcBatch.timeMs
        } else 0.0
        
        if (improvement > 1) {
            println("💡 JDBC Batch is ${String.format("%.1f", improvement)}x faster than JDBC Single")
        }
    }
    
    private fun testUpdateOperations(batchSize: Int) {
        println("\n📊 UPDATE Operations:")
        
        // JDBC Batch Update
        prepareDataForUpdate(batchSize)
        val jdbcUpdate = measureJdbcBatchUpdate(batchSize)
        printTestResult("JDBC Batch Update", jdbcUpdate)
        
        // JPA Dirty Checking Update
        prepareDataForUpdate(batchSize)
        val jpaUpdate = measureJpaEntityUpdate(batchSize)
        printTestResult("JPA Dirty Checking", jpaUpdate)
        
        // JPA Batch Optimized Update (with periodic flush/clear)
        prepareDataForUpdate(batchSize)
        val jpaBatchOptimized = measureJpaBatchOptimizedUpdate(batchSize)
        printTestResult("JPA Batch Optimized", jpaBatchOptimized)
        
        // JPA Bulk Update (JPQL)
        prepareDataForUpdate(batchSize)
        val jpaBulk = measureJpaBulkUpdate(batchSize)
        printTestResult("JPA Bulk Update (JPQL)", jpaBulk)
    }
    
    private fun prepareDataForUpdate(size: Int) {
        cleanupData()
        val entities = generateTestEntities(size)
        jdbcTemplate.batchUpdate(
            "INSERT INTO test_entity (name, amount, description, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
            entities.map { arrayOf(it.name, it.amount, it.description, it.createdAt, it.updatedAt) }
        )
    }
    
    @Transactional
    fun measureJdbcSingleInsert(batchSize: Int): TestResult {
        cleanupData()
        val entities = generateTestEntities(batchSize)
        
        val timeMs = measureTimeMillis {
            entities.forEach { entity ->
                jdbcTemplate.update(
                    "INSERT INTO test_entity (name, amount, description, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                    entity.name, entity.amount, entity.description, entity.createdAt, entity.updatedAt
                )
            }
        }
        
        return createTestResult("JDBC Single Insert", "INSERT", batchSize, timeMs)
    }
    
    @Transactional
    fun measureJdbcBatchInsert(batchSize: Int): TestResult {
        cleanupData()
        val entities = generateTestEntities(batchSize)
        
        val timeMs = measureTimeMillis {
            jdbcTemplate.batchUpdate(
                "INSERT INTO test_entity (name, amount, description, created_at, updated_at) VALUES (?, ?, ?, ?, ?)",
                object : BatchPreparedStatementSetter {
                    override fun setValues(ps: PreparedStatement, i: Int) {
                        val entity = entities[i]
                        ps.setString(1, entity.name)
                        ps.setInt(2, entity.amount)
                        ps.setString(3, entity.description)
                        ps.setTimestamp(4, Timestamp.valueOf(entity.createdAt))
                        ps.setTimestamp(5, Timestamp.valueOf(entity.updatedAt))
                    }
                    
                    override fun getBatchSize(): Int = entities.size
                }
            )
        }
        
        return createTestResult("JDBC Batch Insert", "INSERT", batchSize, timeMs)
    }
    
    @Transactional
    fun measureJpaSingleInsert(batchSize: Int): TestResult {
        cleanupData()
        val entities = generateTestEntities(batchSize)
        
        val timeMs = measureTimeMillis {
            entities.forEach { entity ->
                entityManager.persist(entity)
            }
            entityManager.flush()
        }
        
        return createTestResult("JPA Single Persist", "INSERT", batchSize, timeMs)
    }
    
    @Transactional
    fun measureJpaBatchInsert(batchSize: Int): TestResult {
        cleanupData()
        val entities = generateTestEntities(batchSize)
        
        val timeMs = measureTimeMillis {
            val flushSize = 100
            entities.forEachIndexed { index, entity ->
                entityManager.persist(entity)
                if ((index + 1) % flushSize == 0) {
                    entityManager.flush()
                    entityManager.clear()
                }
            }
            entityManager.flush()
            entityManager.clear()
        }
        
        return createTestResult("JPA Batch Persist", "INSERT", batchSize, timeMs)
    }
    
    @Transactional
    fun measureJdbcBatchUpdate(batchSize: Int): TestResult {
        // 실제 존재하는 ID들을 가져와서 배치 업데이트
        val existingIds = jdbcTemplate.queryForList(
            "SELECT id FROM test_entity ORDER BY id LIMIT ?", 
            Long::class.java, 
            batchSize
        )
        
        val timeMs = measureTimeMillis {
            jdbcTemplate.batchUpdate(
                "UPDATE test_entity SET name = ?, amount = ?, description = ?, updated_at = ? WHERE id = ?",
                existingIds.mapIndexed { index, id ->
                    arrayOf(
                        "Updated_$id", 
                        (index + 1) * 2, 
                        "Updated description for entity $id", 
                        LocalDateTime.now(), 
                        id
                    )
                }
            )
        }
        
        return createTestResult("JDBC Batch Update", "UPDATE", batchSize, timeMs)
    }
    
    @Transactional
    fun measureJpaEntityUpdate(batchSize: Int): TestResult {
        val timeMs = measureTimeMillis {
            val entities = testEntityRepository.findAll().take(batchSize)
            // 변경 감지(Dirty Checking)를 통한 자동 업데이트
            entities.forEach { entity ->
                entity.name = "Updated_${entity.id}"
                entity.amount = entity.amount * 2
                entity.description = "Updated description for entity ${entity.id}"
                entity.updatedAt = LocalDateTime.now()
            }
            // 트랜잭션 종료 시 자동으로 flush 되지만, 시간 측정을 위해 명시적으로 호출
            entityManager.flush()
        }
        
        return createTestResult("JPA Dirty Checking", "UPDATE", batchSize, timeMs)
    }
    
    @Transactional
    fun measureJpaBatchOptimizedUpdate(batchSize: Int): TestResult {
        val timeMs = measureTimeMillis {
            val entities = testEntityRepository.findAll().take(batchSize)
            val flushSize = 100
            // 배치 최적화: 주기적인 flush/clear로 1차 캐시 관리
            entities.forEachIndexed { index, entity ->
                entity.name = "BatchOpt_${entity.id}"
                entity.amount = entity.amount * 2
                entity.description = "Batch optimized update for entity ${entity.id}"
                entity.updatedAt = LocalDateTime.now()
                
                if ((index + 1) % flushSize == 0) {
                    entityManager.flush()
                    entityManager.clear()
                }
            }
            entityManager.flush()
        }
        
        return createTestResult("JPA Batch Optimized", "UPDATE", batchSize, timeMs)
    }
    
    @Transactional
    fun measureJpaBulkUpdate(batchSize: Int): TestResult {
        // 실제 존재하는 ID들을 가져와서 벌크 업데이트
        val existingIds = testEntityRepository.findAll().take(batchSize).map { it.id!! }
        
        val timeMs = measureTimeMillis {
            entityManager.createQuery(
                "UPDATE TestEntity e SET " +
                "e.name = CONCAT('Bulk_', e.id), " +
                "e.amount = e.amount * 2, " +
                "e.description = CONCAT('Bulk updated description for entity ', e.id), " +
                "e.updatedAt = CURRENT_TIMESTAMP " +
                "WHERE e.id IN :ids"
            )
            .setParameter("ids", existingIds)
            .executeUpdate()
        }
        
        return createTestResult("JPA Bulk Update", "UPDATE", batchSize, timeMs)
    }
    
    private fun createTestResult(tech: String, op: String, size: Int, time: Long): TestResult {
        val opsPerSecond = if (time > 0) (size * 1000.0) / time else 0.0
        val result = TestResult(tech, op, size, time, opsPerSecond)
        results.add(result)
        return result
    }
    
    private fun printTestResult(name: String, result: TestResult) {
        println(String.format("  %-25s: %6d ms | %8.2f ops/sec", 
            name, result.timeMs, result.opsPerSecond))
    }
    
    private fun printDetailedSummary() {
        println("\n" + "=".repeat(80))
        println(" DETAILED PERFORMANCE ANALYSIS ")
        println("=".repeat(80))
        
        val groupedByOperation = results.groupBy { it.operation }
        
        groupedByOperation.forEach { (operation, operationResults) ->
            println("\n📈 $operation Performance Analysis:")
            println("-".repeat(60))
            
            val groupedBySize = operationResults.groupBy { it.batchSize }
            groupedBySize.toSortedMap().forEach { (size, sizeResults) ->
                println("\nBatch Size: $size")
                val sorted = sizeResults.sortedBy { it.timeMs }
                sorted.forEachIndexed { index, result ->
                    val emoji = when (index) {
                        0 -> "🥇"
                        1 -> "🥈"
                        2 -> "🥉"
                        else -> "  "
                    }
                    val improvement = if (index > 0) {
                        val fastest = sorted[0].timeMs
                        val current = result.timeMs
                        if (fastest > 0) String.format(" (%.1fx slower)", current.toDouble() / fastest) else ""
                    } else " (fastest)"
                    
                    println(String.format("%s %-25s: %6d ms | %8.2f ops/sec%s",
                        emoji, result.technology, result.timeMs, result.opsPerSecond, improvement))
                }
            }
        }
        
        println("\n" + "=".repeat(80))
        println(" RECOMMENDATIONS ")
        println("=".repeat(80))
        
        println("\n💡 Key Insights:")
        println("   • JDBC Batch operations typically offer the best raw performance")
        println("   • JPA Dirty Checking: Pure change detection without manual optimization")
        println("   • JPA Batch Optimized: Periodic flush/clear to manage 1st level cache")
        println("   • JPA Bulk Update (JPQL): Single query for all updates, bypasses entity lifecycle")
        println("   • Trade-offs: Performance vs convenience vs entity lifecycle features")
        println("   • Choose based on: data volume, performance requirements, and maintainability")
        
        val bestPerformers = results.groupBy { "${it.operation}_${it.batchSize}" }
            .mapValues { it.value.minByOrNull { result -> result.timeMs } }
        
        println("\n🏆 Best Performers by Category:")
        val insertResults = bestPerformers.filterKeys { it.startsWith("INSERT") }
        val updateResults = bestPerformers.filterKeys { it.startsWith("UPDATE") }
        
        println("   INSERT Operations:")
        insertResults.toSortedMap().forEach { (key, result) ->
            val size = key.split("_")[1]
            result?.let { 
                println("     Size $size: ${it.technology} (${it.timeMs}ms)")
            }
        }
        
        println("   UPDATE Operations:")
        updateResults.toSortedMap().forEach { (key, result) ->
            val size = key.split("_")[1]
            result?.let { 
                println("     Size $size: ${it.technology} (${it.timeMs}ms)")
            }
        }
        
        println("\n📊 Technology Summary:")
        val techSummary = results.groupBy { it.technology }
        techSummary.forEach { (tech, techResults) ->
            val avgTime = techResults.map { it.timeMs }.average()
            val avgOps = techResults.map { it.opsPerSecond }.average()
            println("   ${String.format("%-25s", tech)}: Avg ${String.format("%6.0f", avgTime)}ms | ${String.format("%8.0f", avgOps)} ops/sec")
        }
    }
}